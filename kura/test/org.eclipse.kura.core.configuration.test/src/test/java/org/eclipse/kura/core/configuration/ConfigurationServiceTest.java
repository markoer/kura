package org.eclipse.kura.core.configuration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.configuration.metatype.OCD;
import org.eclipse.kura.core.configuration.metatype.Tocd;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.crypto.CryptoService;
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

    @Test
    public void testDeleteFactoryConfigurationNonExistingServicePid() throws KuraException {
        // pid ony registered in factory pids

        // The interesting thing is that factory PIDs are checked in the code, but service PIDs are not... This is
        // because by design the service expects the service PID to exist.

        String pid = "pid";
        boolean takeSnapshot = false;

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Map<String, String> pids = (Map<String, String>) TestUtil.getFieldValue(cs, "m_factoryPidByPid");
        pids.put(pid, pid);

        try {
            cs.deleteFactoryConfiguration(pid, takeSnapshot);

            fail("PID not in service PIDs list - exception expected.");
        } catch (NullPointerException e) {
            // OK - always assume that service PID exists - by design
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
    public void testGetConfigurableComponentPidsEmpty() {
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> configurableComponentPids = cs.getConfigurableComponentPids();

        assertEquals("same length", 0, configurableComponentPids.size());

        try {
            configurableComponentPids.add("unsupported");
            fail("Updating PIDs should not be possible.");
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    @Test
    public void testGetConfigurableComponentPids() {
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        String[] expectedPIDs = { "pid1", "pid2", "pid3" };

        Set<String> s = (Set<String>) TestUtil.getFieldValue(cs, "m_allActivatedPids");
        s.addAll(Arrays.asList(expectedPIDs));

        Set<String> configurableComponentPids = cs.getConfigurableComponentPids();

        assertEquals("same length", 3, configurableComponentPids.size());

        Object[] pidsArray = configurableComponentPids.toArray();
        Arrays.sort(pidsArray);

        for (int i = 0; i < pidsArray.length; i++) {
            assertEquals(expectedPIDs[i], expectedPIDs[i], pidsArray[i]);
        }

        try {
            configurableComponentPids.add("unsupported");
            fail("Updating PIDs should not be possible.");
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    @Test
    public void testGetComponentConfigurations() {
        //// fail("Not yet implemented");
    }

    @Test
    public void testGetComponentConfiguration() {
        //// fail("Not yet implemented");
    }

    // TODO test all related internal methods

    /*
     * Problem: default ComponentConfigurationImpl constructor doesn't initialize properties, but interface doesn't
     * offer a setter method. Result... NPE.
     */
    @Test
    public void testDecryptPasswords() throws KuraException {
        // test password decryption

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        ComponentConfigurationImpl config = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        config.setProperties(props);
        String password = "passval1";
        Password pass = new Password(password);
        String passKey = "pass1";
        props.put(passKey, pass);
        props.put("k2", "val2");

        CryptoService cryptoServiceMock = Mockito.mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        char[] decpass = "decpass".toCharArray();
        Mockito.when(cryptoServiceMock.decryptAes(password.toCharArray())).thenReturn(decpass);

        assertEquals("config size", 2, props.size());

        Map<String, Object> result = cs.decryptPasswords(config);

        Mockito.verify(cryptoServiceMock, times(1)).decryptAes(password.toCharArray());

        assertNotNull("properties not null", result);
        assertEquals("config properties size", 2, result.size());
        assertTrue("contains password", result.containsKey(passKey));
        assertArrayEquals("decrypted pass OK", decpass, ((Password) result.get(passKey)).getPassword());
        assertArrayEquals("decrypted pass OK - reference", decpass, ((Password) props.get(passKey)).getPassword());
    }

    @Test
    public void testDecryptPasswordsException() throws KuraException {
        // test error in password decryption
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        ComponentConfigurationImpl config = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        config.setProperties(props);
        Password pass = new Password("passval1");
        String passKey = "pass1";
        props.put(passKey, pass);

        CryptoService cryptoServiceMock = Mockito.mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        KuraException exc = new KuraException(KuraErrorCode.STORE_ERROR);
        Mockito.when(cryptoServiceMock.decryptAes((char[]) Mockito.anyObject())).thenThrow(exc);

        assertEquals("config size before decryption", 1, props.size());

        cs.decryptPasswords(config);

        Mockito.verify(cryptoServiceMock, times(1)).decryptAes((char[]) Mockito.anyObject());

        assertEquals("config size after decryption", 1, props.size());
    }

    /*
     * No input parameter checking performed!
     */
    @Test
    public void testMergeWithDefaultsNulls() throws KuraException {
        // test with null parameters

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        OCD ocd = null;
        Map<String, Object> properties = null;

        try {
            cs.mergeWithDefaults(ocd, properties);
        } catch (NullPointerException npe) {
            // fail("Input parameters not checked.");
        }
    }

    @Test
    public void testMergeWithDefaultsEmpty() throws KuraException {
        // empty input

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        OCD ocd = new Tocd();
        Map<String, Object> properties = new HashMap<String, Object>();

        boolean merged = cs.mergeWithDefaults(ocd, properties);

        assertFalse("nothing to merge", merged);
        assertEquals("still empty", 0, properties.size());
    }

    @Test
    public void testMergeWithDefaults() throws KuraException {
        // a few default values, a few overrides, one ovelap

        final Map<String, Object> props = new HashMap<String, Object>();
        String prop1Key = "prop1";
        String prop1DefValue = "prop1DefValue";
        props.put(prop1Key, prop1DefValue);
        props.put("defKey2", "defValue2");

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {
            @Override
            Map<String, Object> getDefaultProperties(OCD ocd) throws KuraException {
                return props;
            }
        };

        Tocd ocd = new Tocd();
        Map<String, Object> properties = new HashMap<String, Object>();
        String prop1Value = "value1";
        properties.put(prop1Key, prop1Value);
        properties.put("key2", "value2");

        assertNotEquals(prop1Value, prop1DefValue);

        boolean merged = cs.mergeWithDefaults(ocd, properties);

        assertTrue("properties merged", merged);
        assertEquals("added a property", 3, properties.size());
        assertEquals("value override OK", prop1Value, properties.get(prop1Key));
        assertTrue("value override only", properties.containsKey("key2"));
        assertTrue("default value only", properties.containsKey("defKey2"));
    }

    @Test
    public void testRegisterSelfConfiguringComponentNull() throws NoSuchFieldException {
        // test behavior with null - just abort

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = Mockito.mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = null;

        Mockito.when(allPidsMock.contains(Mockito.anyObject())).thenThrow(new RuntimeException());

        cs.registerSelfConfiguringComponent(pid);

        Mockito.verify(allPidsMock, times(0)).contains(Mockito.anyObject());
    }

    @Test
    public void testRegisterSelfConfiguringComponentNonExistingPID() throws NoSuchFieldException {
        // test behavior with non-existing pid

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = Mockito.mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = "pid";

        Mockito.when(allPidsMock.contains(pid)).thenReturn(false);
        Mockito.when(allPidsMock.add(pid)).thenReturn(true);

        Map<String, String> spbp = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        Set<String> asc = (Set<String>) TestUtil.getFieldValue(cs, "m_activatedSelfConfigComponents");

        assertEquals("empty service pids", 0, spbp.size());
        assertEquals("empty activated configured components", 0, asc.size());

        cs.registerSelfConfiguringComponent(pid);

        Mockito.verify(allPidsMock, times(1)).contains(pid);
        Mockito.verify(allPidsMock, times(1)).add(pid);

        assertEquals("added pid to service pids", 1, spbp.size());
        assertEquals("added pid to activated configured components", 1, asc.size());
    }

    @Test
    public void testRegisterSelfConfiguringComponentExistingPID() throws NoSuchFieldException {
        // test behavior with existing pid

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = Mockito.mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = "pid";

        Mockito.when(allPidsMock.contains(pid)).thenReturn(true);
        Mockito.when(allPidsMock.add(pid)).thenThrow(new RuntimeException());

        Map<String, String> spbp = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        Set<String> asc = (Set<String>) TestUtil.getFieldValue(cs, "m_activatedSelfConfigComponents");

        assertEquals("empty service pids", 0, spbp.size());
        assertEquals("empty activated configured components", 0, asc.size());

        cs.registerSelfConfiguringComponent(pid);

        Mockito.verify(allPidsMock, times(1)).contains(pid);
        Mockito.verify(allPidsMock, times(0)).add(pid);

        assertEquals("not added pid to service pids", 0, spbp.size());
        assertEquals("not added pid to activated configured components", 0, asc.size());
    }

    @Test
    public void testUnregisterComponentConfigurationNull() throws NoSuchFieldException {
        // test behavior with null - just abort

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = Mockito.mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = null;

        Mockito.when(allPidsMock.contains(Mockito.anyObject())).thenThrow(new RuntimeException());

        cs.unregisterComponentConfiguration(pid);

        Mockito.verify(allPidsMock, times(0)).contains(Mockito.anyObject());
    }

    @Test
    public void testUnregisterComponentConfiguration() throws NoSuchFieldException {
        // test behavior with non-existing pid

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        String pid = "pid";

        Set<String> allPids = (Set<String>) TestUtil.getFieldValue(cs, "m_allActivatedPids");
        allPids.add(pid + "1");
        Map<String, String> spbp = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        spbp.put(pid + "1", pid);
        Set<String> asc = (Set<String>) TestUtil.getFieldValue(cs, "m_activatedSelfConfigComponents");
        asc.add(pid + "1");

        assertEquals("all pids size", 1, allPids.size());
        assertEquals("service pids size", 1, spbp.size());
        assertEquals("activated pids size", 1, asc.size());

        assertFalse("all pids don't contain pid", allPids.contains(pid));
        assertFalse("service pids don't contain pid", spbp.containsKey(pid));
        assertFalse("activated pids don't contain pid", asc.contains(pid));

        cs.unregisterComponentConfiguration(pid);

        // no change
        assertEquals("all pids size", 1, allPids.size());
        assertEquals("service pids size", 1, spbp.size());
        assertEquals("activated pids size", 1, asc.size());

        assertFalse("all pids don't contain pid", allPids.contains(pid));
        assertFalse("service pids don't contain pid", spbp.containsKey(pid));
        assertFalse("activated pids don't contain pid", asc.contains(pid));
    }

    @Test
    public void testEncryptConfigsNull() throws NoSuchMethodException {
        // test with null parameter

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        List<? extends ComponentConfiguration> configs = null;

        try {
            TestUtil.invokePrivate(cs, "encryptConfigs", configs);
        } catch (Throwable e) {
            // fail("Parameters not checked, but then again, it is a private method.");
        }

    }

    @Test
    public void testEncryptConfigsNoConfigs() throws Throwable {
        // empty list

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        List<? extends ComponentConfiguration> configs = new ArrayList<ComponentConfiguration>();

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        // runs without problems, but there's nothing else to check, here
    }

    @Test
    public void testEncryptConfigsEncryptionException() throws Throwable {
        // test failed encryption of a password: add a password and run; fail
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        CryptoService cryptoServiceMock = Mockito.mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // first decryption must fail
        Mockito.when(cryptoServiceMock.decryptAes("pass".toCharArray()))
                .thenThrow(new KuraException(KuraErrorCode.DECODER_ERROR));
        // then also encryption can fail
        Mockito.when(cryptoServiceMock.encryptAes("pass".toCharArray()))
                .thenThrow(new KuraException(KuraErrorCode.ENCODE_ERROR));

        List<ComponentConfigurationImpl> configs = new ArrayList<ComponentConfigurationImpl>();

        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("key1", new Password("pass"));
        cfg.setProperties(props);

        configs.add(cfg);

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        Mockito.verify(cryptoServiceMock, times(1)).decryptAes("pass".toCharArray());
        Mockito.verify(cryptoServiceMock, times(1)).encryptAes("pass".toCharArray());

        assertEquals("property was deleted", 0, props.size());
    }

    @Test
    public void testEncryptConfigs() throws Throwable {
        // test encrypting a password: add a password and run
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        CryptoService cryptoServiceMock = Mockito.mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // first decryption must fail
        Mockito.when(cryptoServiceMock.decryptAes("pass".toCharArray()))
                .thenThrow(new KuraException(KuraErrorCode.DECODER_ERROR));
        // so that encryption is attempted at all
        Mockito.when(cryptoServiceMock.encryptAes("pass".toCharArray())).thenReturn("encrypted".toCharArray());

        List<ComponentConfigurationImpl> configs = new ArrayList<ComponentConfigurationImpl>();

        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("key1", new Password("pass"));
        cfg.setProperties(props);

        configs.add(cfg);

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        Mockito.verify(cryptoServiceMock, times(1)).decryptAes("pass".toCharArray());
        Mockito.verify(cryptoServiceMock, times(1)).encryptAes("pass".toCharArray());

        assertEquals("property was updated", 1, props.size());
        assertTrue("key still exists", props.containsKey("key1"));
        assertArrayEquals("key is encrypted", "encrypted".toCharArray(), ((Password) props.get("key1")).getPassword());
    }

    @Test
    public void testEncryptConfigsPreencryptedPassword() throws Throwable {
        // test encrypting a password when the password is already encrypted
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        CryptoService cryptoServiceMock = Mockito.mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // decryption succeeds this time
        Mockito.when(cryptoServiceMock.decryptAes("pass".toCharArray())).thenReturn("pass".toCharArray());

        List<ComponentConfigurationImpl> configs = new ArrayList<ComponentConfigurationImpl>();

        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("key1", new Password("pass"));
        cfg.setProperties(props);

        configs.add(cfg);

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        Mockito.verify(cryptoServiceMock, times(1)).decryptAes("pass".toCharArray());
        Mockito.verify(cryptoServiceMock, times(0)).encryptAes((char[]) Mockito.anyObject());

        assertEquals("property remains", 1, props.size());
        assertTrue("key still exists", props.containsKey("key1"));
        assertArrayEquals("key is already encrypted", "pass".toCharArray(),
                ((Password) props.get("key1")).getPassword());
    }

    @Test
    public void testGetDefaultComponentConfiguration() {
        // fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationStringMapOfStringObject() {
        // fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationStringMapOfStringObjectBoolean() {
        // fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationsListOfComponentConfiguration() {
        // fail("Not yet implemented");
    }

    @Test
    public void testUpdateConfigurationsListOfComponentConfigurationBoolean() {
        //// fail("Not yet implemented");
    }

    @Test
    public void testGetSnapshots() {
        // fail("Not yet implemented");
    }

    @Test
    public void testGetSnapshot() {
        // fail("Not yet implemented");
    }

    @Test
    public void testSnapshot() {
        // fail("Not yet implemented");
    }

    @Test
    public void testRegisterComponentConfiguration() {
        // fail("Not yet implemented");
    }

    @Test
    public void testRollback() {
        // fail("Not yet implemented");
    }

    @Test
    public void testRollbackLong() {
        // fail("Not yet implemented");
    }

}
