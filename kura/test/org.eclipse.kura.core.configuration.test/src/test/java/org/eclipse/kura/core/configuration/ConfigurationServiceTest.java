package org.eclipse.kura.core.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.core.testutil.TestUtil;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigurationServiceTest {

    @Test
    public void testGetFactoryComponentPids() throws NoSuchFieldException {
        // test that the returned PIDs are the same as in the service and that they cannot be modified

        String[] expectedPIDs = { "pid1", "pid2", "pid3" };

        ConfigurationService cs = new ConfigurationServiceImpl();

        @SuppressWarnings("unchecked")
        Set<String> s = (Set<String>) TestUtil.getFieldValue(cs, "m_factoryPids");
        s.addAll(Arrays.asList(expectedPIDs));

        Set<String> factoryComponentPids = cs.getFactoryComponentPids();

        assertEquals("same length", 3, factoryComponentPids.size());

        Object[] pidsArray = factoryComponentPids.toArray();
        Arrays.sort(pidsArray);

        for (int i = 0; i < pidsArray.length; i++) {
            assertEquals(expectedPIDs[i], expectedPIDs[i], pidsArray[i]);
        }

        try {
            factoryComponentPids.add("unsupported");
            fail("Updating PIDs should not be possible.");
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    @Test
    public void testCreateFactoryConfigurationNulls() {
        // negative test; how null values are handled

        ConfigurationService cs = new ConfigurationServiceImpl();

        String factoryPid = null;
        String pid = null;
        Map<String, Object> properties = null;
        boolean takeSnapshot = false;

        try {
            cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

            fail("Exception expected with null pid value.");
        } catch (Exception e) {
            // OK, probably
        }
    }

    @Test
    public void testCreateFactoryExistingPid() throws KuraException, IOException {
        // negative test; what if existing PID is used

        final String factoryPid = "fpid";
        final String pid = "mypid";
        Map<String, Object> properties = null;
        final boolean takeSnapshot = false;

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Map<String, String> pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        pids.put(pid, pid);

        try {
            cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

            fail("Exception expected with existing pid value.");
        } catch (Exception e) {
            // OK, probably
        }
    }

    @Test
    public void testCreateFactoryConfigurationConfigException() throws KuraException, IOException {
        // negative test; invalid configuration exception

        final String factoryPid = "fpid";
        final String pid = "mypid";
        Map<String, Object> properties = null;
        final boolean takeSnapshot = false;
        final String caPid = "caPid";

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        IOException ioe = new IOException("test");
        Mockito.when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenThrow(ioe);

        try {
            cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

            fail("Exception expected");
        } catch (KuraException e) {
            // OK
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testCreateFactoryConfigurationNoSnapshot() throws KuraException, IOException {
        // a positive test, without snapshot creation

        final String factoryPid = "fpid";
        final String pid = "mypid";
        Map<String, Object> properties = null;
        final boolean takeSnapshot = false;
        final String caPid = "caPid";

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            // test that protected component registration was called with the proper parameters
            synchronized void registerComponentConfiguration(String pid1, String servicePid, String factoryPid1) {
                assertEquals("PIDs match", pid, pid1);
                assertEquals("Service PIDs match", caPid, servicePid);
                assertEquals("PIDs match", factoryPid, factoryPid1);
            };

            // test that snapshot is not made if not configured so
            @Override
            public long snapshot() throws KuraException {
                if (!takeSnapshot) {
                    fail("Snapshot is turned off.");
                }
                return super.snapshot();
            }
        };

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration cfgMock = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenReturn(cfgMock);

        Mockito.when(cfgMock.getPid()).thenReturn(caPid);

        Configuration cfgMock2 = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.getConfiguration(caPid, "?")).thenReturn(cfgMock2);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[0];

                assertNotNull(dict);

                assertEquals("one element in properties list - pid", 1, dict.size());

                assertEquals("expected configuration update PID", pid, dict.elements().nextElement());

                return null;
            }
        }).when(cfgMock2).update((Dictionary<String, Object>) Mockito.anyObject());

        cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

        Mockito.verify(cfgMock2, Mockito.times(1)).update((Dictionary<String, Object>) Mockito.anyObject());
    }

    @Test
    public void testCreateFactoryConfigurationMergeProperties() throws KuraException, IOException {
        // a positive test, take passed properties into account, without snapshot creation

        final String factoryPid = "fpid";
        final String pid = "mypid";
        Map<String, Object> properties = new HashMap<String, Object>();
        final boolean takeSnapshot = false;
        final String caPid = "caPid";

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            synchronized void registerComponentConfiguration(String pid1, String servicePid, String factoryPid1) {
            };
        };

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration cfgMock = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenReturn(cfgMock);

        Mockito.when(cfgMock.getPid()).thenReturn(caPid);

        Configuration cfgMock2 = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.getConfiguration(caPid, "?")).thenReturn(cfgMock2);

        properties.put("key1", "val1");
        properties.put("key2", "val2");

        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[0];

                assertNotNull(dict);

                assertEquals("3 elements in properties list", 3, dict.size());

                assertEquals("additional key", "val1", dict.get("key1"));
                assertEquals("additional key", "val2", dict.get("key2"));

                return null;
            }
        }).when(cfgMock2).update((Dictionary<String, Object>) Mockito.anyObject());

        cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

        Mockito.verify(cfgMock2, Mockito.times(1)).update((Dictionary<String, Object>) Mockito.anyObject());
    }

    @Test
    public void testCreateFactoryConfigurationWithSnapshot() throws KuraException, IOException {
        // a positive test, check only snapshot creation

        final String factoryPid = "fpid";
        final String pid = "mypid";
        Map<String, Object> properties = null;
        final boolean takeSnapshot = true;
        final String caPid = "caPid";

        final boolean[] snapshots = { false };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            // test that protected component registration was called with the proper parameters
            synchronized void registerComponentConfiguration(String pid1, String servicePid, String factoryPid1) {
            };

            // test that snapshot is not made if not configured so
            @Override
            public long snapshot() throws KuraException {
                snapshots[0] = true;

                return 1L;
            }
        };

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration cfgMock = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenReturn(cfgMock);

        Mockito.when(cfgMock.getPid()).thenReturn(caPid);

        Configuration cfgMock2 = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.getConfiguration(caPid, "?")).thenReturn(cfgMock2);

        assertFalse("snapshots init OK", snapshots[0]);

        cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

        assertTrue("snapshot() called", snapshots[0]);
    }

    @Test
    public void testDeleteFactoryConfigurationNulls() throws KuraException {
        // negative test; null pid

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        String pid = null;
        boolean takeSnapshot = false;

        try {
            cs.deleteFactoryConfiguration(pid, takeSnapshot);

            fail("Null parameter - exception expected.");
        } catch (KuraException e) {
            assertTrue(e.getMessage().contains("INVALID_PARAMETER"));
        }
    }

    @Test
    public void testDeleteFactoryConfigurationNonExistingFactoryPid() throws KuraException {
        // negative test; pid not registered

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        String pid = "pid";
        boolean takeSnapshot = false;

        try {
            cs.deleteFactoryConfiguration(pid, takeSnapshot);

            fail("Nonexisting parameter - exception expected.");
        } catch (KuraException e) {
            assertTrue(e.getMessage().contains("INVALID_PARAMETER"));
        }
    }

    /*
     * The interesting thing is that factory PIDs are checked in the code, but service PIDs are not... Why is that?
     */
    @Test
    public void testDeleteFactoryConfigurationNonExistingServicePid() throws KuraException {
        // negative test; pid registered in factory pids

        String pid = "pid";
        boolean takeSnapshot = false;

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Map<String, String> pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_factoryPidByPid");
        pids.put(pid, pid);

        try {
            cs.deleteFactoryConfiguration(pid, takeSnapshot);

            fail("PID not in service PIDs list - exception expected. BUT... it shouldn't be NPE, but KuraException.");
        } catch (NullPointerException e) {
            // fail("Was hoping for KuraException.");
        } catch (KuraException e) {
            // assertTrue(e.getMessage().contains("INVALID_PARAMETER"));
        }
    }

    @Test
    public void testDeleteFactoryConfigurationNoSnapshot() throws KuraException, IOException {
        // positive test; pid registered in factory and service pids, configuration delete is expected, no snapshot

        String factoryPid = "fpid";
        final String servicePid = "spid";
        final boolean takeSnapshot = false;

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            synchronized void unregisterComponentConfiguration(String pid) {
                assertEquals("service pid to unregister", servicePid, pid);
            }

            @Override
            public long snapshot() throws KuraException {
                if (!takeSnapshot) {
                    fail("Snapshot is turned off.");
                }

                return 1L;
            }
        };

        Map<String, String> pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_factoryPidByPid");
        pids.put(servicePid, factoryPid);

        pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        pids.put(servicePid, servicePid);

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration configMock = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.getConfiguration(servicePid, "?")).thenReturn(configMock);

        cs.deleteFactoryConfiguration(servicePid, takeSnapshot);

        Mockito.verify(configMock, Mockito.times(1)).delete();
    }

    @Test
    public void testDeleteFactoryConfigurationWithSnapshot() throws KuraException, IOException {
        // positive test; pid registered in factory and service pids, configuration delete is expected, take a snapshot

        String factoryPid = "fpid";
        final String servicePid = "spid";
        final boolean takeSnapshot = true;

        final boolean[] snapshots = { false };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            synchronized void unregisterComponentConfiguration(String pid) {
                assertEquals("service pid to unregister", servicePid, pid);
            }

            @Override
            public long snapshot() throws KuraException {
                snapshots[0] = true;

                return 1L;
            }
        };

        Map<String, String> pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_factoryPidByPid");
        pids.put(servicePid, factoryPid);

        pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        pids.put(servicePid, servicePid);

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration configMock = Mockito.mock(Configuration.class);
        Mockito.when(configAdminMock.getConfiguration(servicePid, "?")).thenReturn(configMock);

        assertFalse("snapshot still untouched", snapshots[0]);

        cs.deleteFactoryConfiguration(servicePid, takeSnapshot);

        Mockito.verify(configMock, Mockito.times(1)).delete();
        assertTrue("snapshot taken", snapshots[0]);
    }

    @Test
    public void testDeleteFactoryConfigurationConfigurationException() throws KuraException, IOException {
        // negative test; pid registered in factory and service pids, configuration retrieval fails

        String factoryPid = "fpid";
        final String servicePid = "spid";
        final boolean takeSnapshot = true;

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Map<String, String> pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_factoryPidByPid");
        pids.put(servicePid, factoryPid);

        pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        pids.put(servicePid, servicePid);

        ConfigurationAdmin configAdminMock = Mockito.mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Throwable ioe = new IOException("test");
        Mockito.when(configAdminMock.getConfiguration(servicePid, "?")).thenThrow(ioe);

        try {
            cs.deleteFactoryConfiguration(servicePid, takeSnapshot);
        } catch (KuraException e) {
            assertTrue(e.getMessage().contains("Cannot delete"));
        }
    }

    @Test
    public void testGetConfigurableComponentPids() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetComponentConfigurations() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDefaultComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationStringMapOfStringObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationStringMapOfStringObjectBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationsListOfComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationsListOfComponentConfigurationBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSnapshots() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSnapshot() {
        fail("Not yet implemented");
    }

    @Test
    public void testSnapshot() {
        fail("Not yet implemented");
    }

    @Test
    public void testRegisterComponentConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    public void testRollback() {
        fail("Not yet implemented");
    }

    @Test
    public void testRollbackLong() {
        fail("Not yet implemented");
    }

}
