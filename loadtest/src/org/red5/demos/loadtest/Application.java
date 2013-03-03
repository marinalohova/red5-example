package org.red5.demos.loadtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.commons.lang3.RandomStringUtils;
import org.red5.io.amf3.ByteArray;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.so.ISharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;
import org.red5.server.api.stream.IServerStream;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Application extends ApplicationAdapter implements ApplicationContextAware {

	protected Logger log = Red5LoggerFactory.getLogger(Application.class, "loadtest");

	private static ApplicationContext applicationContext;

	private Timer timer = new Timer();

	private IScope appScope;

	private IServerStream serverStream;

	{
		log.info("loadtest created");
	}

	/** {@inheritDoc} */
	@Override
	public boolean appStart(IScope scope) {
		log.info("loadtest appStart");
		appScope = scope;

		createSharedObject(appScope, "loadtestSO", true);
		ISharedObject so = getSharedObject(appScope, "loadtestSO");

		// add a listener
		so.addSharedObjectListener(new LoadTestSharedObjectListener());

		// add a handler
		so.registerServiceHandler(new LoadTestSharedObjectHandler());

		// start a job to update an SO on interval
		timer.scheduleAtFixedRate(new SOUpdateJob(), 1000, 15000);

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean appConnect(IConnection conn, Object[] params) {
		log.info("loadtest appConnect");
		return super.appConnect(conn, params);
	}

	/** {@inheritDoc} */
	@Override
	public void appDisconnect(IConnection conn) {
		if (appScope == conn.getScope() && serverStream != null) {
			serverStream.close();
		}
		super.appDisconnect(conn);
	}

	/**
	 * Fill a ByteArray with random bytes up to the given number of entries.
	 * 
	 * @param numberOfEntries
	 * @return ByteArray of random bytes
	 */
	public ByteArray getByteArray(int numberOfEntries) {
		log.debug("getByteArray: {}", numberOfEntries);
		Random rnd = new Random();
		ByteArray ba = new ByteArray();
		for (int i = 0; i < numberOfEntries; i++) {
			ba.writeByte((byte) rnd.nextInt(128));
		}
		return ba;
	}

	/**
	 * Accepts a ByteArray of objects and echo's it back to the client.
	 * 
	 * @param ba
	 * @return ByteArray
	 */
	public ByteArray echoByteArray(ByteArray ba) {
		log.debug("echoByteArray: {}", ba);
		for (int i = 0; i < ba.length(); i++) {
			log.debug("Byte: {}", ba.readByte());
		}
		return ba;
	}

	/**
	 * Accepts a vector of objects and echo's it back to the client.
	 * 
	 * @param vector
	 * @return Vector
	 */
	public Vector<Object> echoVector(Vector<Object> vector) {
		log.debug("echoVector: {}", vector);
		for (Object obj : vector) {
			log.debug("Element: {}", obj);
		}
		return vector;
	}

	public void updateSO() {
		// get scope

		ISharedObject so = getSharedObject(scope, "loadtestSO");
		so.setAttribute("count", "changed value");

		// if multiple updates
		// ISharedObject so = getSharedObject(scope, "loadtestSO");
		// so.beginUpdate();
		// so.setAttribute("count", "changed value");
		// so.setAttribute("ts", System.currentTimeMillis());
		// so.endUpdate();
	}

	/**
	 * @param applicationContext
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		Application.applicationContext = applicationContext;
		log.trace("setApplicationContext {}", Application.applicationContext);
	}

	class LoadTestSharedObjectListener implements ISharedObjectListener {

		@Override
		public void onSharedObjectClear(ISharedObjectBase so) {
			log.debug("onSharedObjectClear {}", so);
		}

		@Override
		public void onSharedObjectConnect(ISharedObjectBase so) {
			log.debug("onSharedObjectConnect {}", so);
		}

		@Override
		public void onSharedObjectDelete(ISharedObjectBase so, String key) {
			log.debug("onSharedObjectDelete key: {} {}", key, so);
		}

		@Override
		public void onSharedObjectDisconnect(ISharedObjectBase so) {
			log.debug("onSharedObjectDisconnect {}", so);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onSharedObjectSend(ISharedObjectBase so, String method, List params) {
			// The handler <method> of the shared object <so> was called
			// with the parameters <params>
			log.debug("onSharedObjectSend {} method: {}", so, method);
		}

		@Override
		public void onSharedObjectUpdate(ISharedObjectBase so, IAttributeStore arg1) {
			// randomInteger
			log.debug("onSharedObjectUpdate {} {}", so, arg1);

		}

		@Override
		public void onSharedObjectUpdate(ISharedObjectBase so, Map<String, Object> arg1) {
			log.debug("onSharedObjectUpdate {} {}", so, arg1);

		}

		@Override
		public void onSharedObjectUpdate(ISharedObjectBase so, String key, Object value) {
			// The attribute <key> of the shared object <so>
			// was changed to <value>
			log.debug("onSharedObjectUpdate key: {} value: {} {}", new Object[] { key, value, so });

		}

		// Other methods as described in the interface...
	}

	// remote_so.send(<handler>, <args>)
	class LoadTestSharedObjectHandler {

		public void hello(String arg1) {
			log.debug("Handler got a hello {}", arg1);
		}

	}

	private final class SOUpdateJob extends TimerTask {

		public void run() {
			log.debug("Updating SO");
			try {
				List<String> list = new ArrayList<String>();
				// fill with some random strings
				for (int i = 0; i < 9; i++) {
					list.add(RandomStringUtils.randomAlphanumeric(8));
				}
				log.debug("Random strings: {}", list);
				// get or create the SO
				IScope webScope = (IScope) applicationContext.getBean("web.scope");
				log.debug("SO scope: {}", webScope);
				ISharedObject strListSO = getSharedObject(webScope, "strList");
				if (strListSO == null) {
					log.debug("Creating SO for list data");
					createSharedObject(webScope, "strList", true);
					log.debug("Creating list: {}", strListSO);
					strListSO = getSharedObject(webScope, "strList");
				}
				log.debug("String list SO: {}", strListSO);
				// update the SO
				strListSO.beginUpdate();
				strListSO.setAttribute("object", list);
				strListSO.endUpdate();
			} catch (Exception e) {
				log.warn("Exception pushing hub list", e);
			}
		}

	}
}
