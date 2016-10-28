package org.eclipse.kura.core.data;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.kura.KuraConnectException;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.core.db.HsqlDbServiceImpl;
import org.eclipse.kura.data.DataTransportService;
import org.eclipse.kura.data.transport.listener.DataTransportListener;
import org.eclipse.kura.db.DbService;
import org.eclipse.kura.status.CloudConnectionStatusComponent;
import org.eclipse.kura.status.CloudConnectionStatusService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.component.ComponentContext;

public class DataServiceImplTest {

    @BeforeClass
    public static void setUp() {
    }

    @Test
	public void testActivateMissingProperties() {
		DataServiceImpl ds = new DataServiceImpl(); 

		ComponentContext componentContext = Mockito.mock(ComponentContext.class);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(ConfigurationService.KURA_SERVICE_PID, "pid");
		
		try {
			ds.activate(componentContext, properties);
			fail("Exception expected.");
		} catch (NullPointerException e) {
			// OK
		}
	}
	
	@Test
	public void testActivateNoDBService() {
		DataServiceImpl ds = new DataServiceImpl(); 
		
		ComponentContext componentContextMock = Mockito.mock(ComponentContext.class);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(ConfigurationService.KURA_SERVICE_PID, "pid");
		properties.put("store.housekeeper-interval", 1);
        properties.put("store.purge-age", 1);
        properties.put("store.capacity", 1);
        
		try {
			ds.activate(componentContextMock, properties);
			fail("Exception expected.");
		} catch (NullPointerException e) {
			// OK
		}
	}
	
	@Test
	public void testActivate() throws SQLException, KuraConnectException {
		final DataServiceImpl ds = new DataServiceImpl(); 
		
		ComponentContext componentContextMock = Mockito.mock(ComponentContext.class);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(ConfigurationService.KURA_SERVICE_PID, "pid");
		properties.put("store.housekeeper-interval", 1);
		properties.put("store.purge-age", 1);
		properties.put("store.capacity", 1);

		properties.put("connect.auto-on-startup", false);
		properties.put("connect.retry-interval", 1);
		
		DbService dbServiceMock = Mockito.mock(HsqlDbServiceImpl.class);
		Connection connectionMock = Mockito.mock(Connection.class);
		Mockito.when(dbServiceMock.getConnection()).thenReturn(connectionMock);

		PreparedStatement statementMock = Mockito.mock(PreparedStatement.class);
		Mockito.when(connectionMock.prepareStatement(Mockito.anyString())).thenReturn(statementMock);

		ResultSet rsMock = Mockito.mock(ResultSet.class);
		Mockito.when(statementMock.executeQuery()).thenReturn(rsMock);

		Mockito.when(rsMock.next()).thenReturn(false);

		ds.setDbService(dbServiceMock);
		
		CloudConnectionStatusService cloudConnectionStatusServiceMock = Mockito
				.mock(CloudConnectionStatusService.class);
		ds.setCloudConnectionStatusService(cloudConnectionStatusServiceMock);
		
		// verify that service has been registered with cloud connection status service
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(invocation.getArguments()[0], ds);
				return null;
			}
		}).when(cloudConnectionStatusServiceMock).register((CloudConnectionStatusComponent) Mockito.anyObject());

		DataTransportService dataTransportServiceMock = Mockito.mock(DataTransportService.class);
		ds.setDataTransportService(dataTransportServiceMock);
		
		// verify that service has been added as a listener
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(invocation.getArguments()[0], ds);
				return null;
			}
		}).when(dataTransportServiceMock).addDataTransportListener((DataTransportListener) Mockito.anyObject());
		
		// verify that connect attempt has been made
//		Mockito.doAnswer(new Answer() {
//			@Override
//			public Object answer(InvocationOnMock invocation) throws Throwable {
//				return null;
//			}
//		}).when(dataTransportServiceMock).connect();

		ds.activate(componentContextMock, properties);
		
		Mockito.verify(dataTransportServiceMock).addDataTransportListener((DataTransportListener) Mockito.anyObject());;

		assertNotNull("reconnect executor not null", TestUtil.getFieldValue(ds, "m_reconnectExecutor"));
		assertNotNull("publisher executor not null", TestUtil.getFieldValue(ds, "m_publisherExecutor"));
		assertNotNull("congestion executor not null", TestUtil.getFieldValue(ds, "m_congestionExecutor"));
		assertNotNull("store not null", TestUtil.getFieldValue(ds, "m_store"));
	}

//	@Test
//	public void testUpdated() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testDeactivate() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetDataTransportService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUnsetDataTransportService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetDbService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUnsetDbService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetCloudConnectionStatusService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUnsetCloudConnectionStatusService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddDataServiceListener() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testRemoveDataServiceListener() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnConnectionEstablished() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnDisconnecting() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnDisconnected() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnConfigurationUpdating() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnConfigurationUpdated() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnConnectionLost() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnMessageArrived() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testOnMessageConfirmed() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConnect() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsConnected() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsAutoConnectEnabled() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetRetryInterval() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testDisconnect() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSubscribe() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUnsubscribe() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPublish() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetUnpublishedMessageIds() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetInFlightMessageIds() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetDroppedInFlightMessageIds() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNotificationPriority() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNotificationStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetNotificationStatus() {
//		fail("Not yet implemented");
//	}

}
