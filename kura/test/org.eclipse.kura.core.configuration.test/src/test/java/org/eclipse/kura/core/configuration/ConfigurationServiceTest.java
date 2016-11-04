package org.eclipse.kura.core.configuration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.configuration.metatype.OCD;
import org.eclipse.kura.core.configuration.metatype.Tocd;
import org.eclipse.kura.core.configuration.util.XmlUtil;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.crypto.CryptoService;
import org.eclipse.kura.system.SystemService;
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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        IOException ioe = new IOException("test");
        when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenThrow(ioe);

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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration cfgMock = mock(Configuration.class);
        when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenReturn(cfgMock);

        when(cfgMock.getPid()).thenReturn(caPid);

        Configuration cfgMock2 = mock(Configuration.class);
        when(configAdminMock.getConfiguration(caPid, "?")).thenReturn(cfgMock2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[0];

                assertNotNull(dict);

                assertEquals("one element in properties list - pid", 1, dict.size());

                assertEquals("expected configuration update PID", pid, dict.elements().nextElement());

                return null;
            }
        }).when(cfgMock2).update((Dictionary<String, Object>) anyObject());

        cs.createFactoryConfiguration(factoryPid, pid, properties, takeSnapshot);

        verify(cfgMock2, times(1)).update((Dictionary<String, Object>) anyObject());
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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration cfgMock = mock(Configuration.class);
        when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenReturn(cfgMock);

        when(cfgMock.getPid()).thenReturn(caPid);

        Configuration cfgMock2 = mock(Configuration.class);
        when(configAdminMock.getConfiguration(caPid, "?")).thenReturn(cfgMock2);

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

        verify(cfgMock2, Mockito.times(1)).update((Dictionary<String, Object>) Mockito.anyObject());
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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration cfgMock = mock(Configuration.class);
        when(configAdminMock.createFactoryConfiguration(factoryPid, null)).thenReturn(cfgMock);

        when(cfgMock.getPid()).thenReturn(caPid);

        Configuration cfgMock2 = mock(Configuration.class);
        when(configAdminMock.getConfiguration(caPid, "?")).thenReturn(cfgMock2);

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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration configMock = mock(Configuration.class);
        when(configAdminMock.getConfiguration(servicePid, "?")).thenReturn(configMock);

        cs.deleteFactoryConfiguration(servicePid, takeSnapshot);

        verify(configMock, Mockito.times(1)).delete();
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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Configuration configMock = mock(Configuration.class);
        when(configAdminMock.getConfiguration(servicePid, "?")).thenReturn(configMock);

        assertFalse("snapshot still untouched", snapshots[0]);

        cs.deleteFactoryConfiguration(servicePid, takeSnapshot);

        verify(configMock, Mockito.times(1)).delete();
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

        ConfigurationAdmin configAdminMock = mock(ConfigurationAdmin.class);
        cs.setConfigurationAdmin(configAdminMock);

        Throwable ioe = new IOException("test");
        when(configAdminMock.getConfiguration(servicePid, "?")).thenThrow(ioe);

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
        // fail("Not yet implemented");
    }

    @Test
    public void testGetComponentConfiguration() {
        // fail("Not yet implemented");
    }

    /*
     * FIXME: default ComponentConfigurationImpl constructor doesn't initialize properties, but interface doesn't
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

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        char[] decpass = "decpass".toCharArray();
        when(cryptoServiceMock.decryptAes(password.toCharArray())).thenReturn(decpass);

        assertEquals("config size", 2, props.size());

        Map<String, Object> result = cs.decryptPasswords(config);

        verify(cryptoServiceMock, times(1)).decryptAes(password.toCharArray());

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

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        KuraException exc = new KuraException(KuraErrorCode.STORE_ERROR);
        when(cryptoServiceMock.decryptAes((char[]) Mockito.anyObject())).thenThrow(exc);

        assertEquals("config size before decryption", 1, props.size());

        cs.decryptPasswords(config);

        verify(cryptoServiceMock, times(1)).decryptAes((char[]) Mockito.anyObject());

        assertEquals("config size after decryption", 1, props.size());
    }

    /*
     * FIXME: No input parameter checking performed!
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

        Set<String> allPidsMock = mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = null;

        when(allPidsMock.contains(Mockito.anyObject())).thenThrow(new RuntimeException());

        cs.registerSelfConfiguringComponent(pid);

        verify(allPidsMock, times(0)).contains(Mockito.anyObject());
    }

    @Test
    public void testRegisterSelfConfiguringComponentNonExistingPID() throws NoSuchFieldException {
        // test behavior with non-existing pid

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = "pid";

        when(allPidsMock.contains(pid)).thenReturn(false);
        when(allPidsMock.add(pid)).thenReturn(true);

        Map<String, String> spbp = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        Set<String> asc = (Set<String>) TestUtil.getFieldValue(cs, "m_activatedSelfConfigComponents");

        assertEquals("empty service pids", 0, spbp.size());
        assertEquals("empty activated configured components", 0, asc.size());

        cs.registerSelfConfiguringComponent(pid);

        verify(allPidsMock, times(1)).contains(pid);
        verify(allPidsMock, times(1)).add(pid);

        assertEquals("added pid to service pids", 1, spbp.size());
        assertEquals("added pid to activated configured components", 1, asc.size());
    }

    @Test
    public void testRegisterSelfConfiguringComponentExistingPID() throws NoSuchFieldException {
        // test behavior with existing pid

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = "pid";

        when(allPidsMock.contains(pid)).thenReturn(true);
        when(allPidsMock.add(pid)).thenThrow(new RuntimeException());

        Map<String, String> spbp = (Map<String, String>) TestUtil.getFieldValue(cs, "m_servicePidByPid");
        Set<String> asc = (Set<String>) TestUtil.getFieldValue(cs, "m_activatedSelfConfigComponents");

        assertEquals("empty service pids", 0, spbp.size());
        assertEquals("empty activated configured components", 0, asc.size());

        cs.registerSelfConfiguringComponent(pid);

        verify(allPidsMock, times(1)).contains(pid);
        verify(allPidsMock, times(0)).add(pid);

        assertEquals("not added pid to service pids", 0, spbp.size());
        assertEquals("not added pid to activated configured components", 0, asc.size());
    }

    @Test
    public void testUnregisterComponentConfigurationNull() throws NoSuchFieldException {
        // test behavior with null - just abort

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        Set<String> allPidsMock = mock(Set.class);
        TestUtil.setFieldValue(cs, "m_allActivatedPids", allPidsMock);

        String pid = null;

        when(allPidsMock.contains(Mockito.anyObject())).thenThrow(new RuntimeException());

        cs.unregisterComponentConfiguration(pid);

        verify(allPidsMock, times(0)).contains(Mockito.anyObject());
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

    /*
     * TODO: maybe fix implementation
     */
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

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // first decryption must fail
        when(cryptoServiceMock.decryptAes("pass".toCharArray()))
                .thenThrow(new KuraException(KuraErrorCode.DECODER_ERROR));
        // then also encryption can fail
        when(cryptoServiceMock.encryptAes("pass".toCharArray()))
                .thenThrow(new KuraException(KuraErrorCode.ENCODE_ERROR));

        List<ComponentConfigurationImpl> configs = new ArrayList<ComponentConfigurationImpl>();

        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("key1", new Password("pass"));
        cfg.setProperties(props);

        configs.add(cfg);

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        verify(cryptoServiceMock, times(1)).decryptAes("pass".toCharArray());
        verify(cryptoServiceMock, times(1)).encryptAes("pass".toCharArray());

        assertEquals("property was deleted", 0, props.size());
    }

    @Test
    public void testEncryptConfigs() throws Throwable {
        // test encrypting a password: add a password and run
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // first decryption must fail
        when(cryptoServiceMock.decryptAes("pass".toCharArray()))
                .thenThrow(new KuraException(KuraErrorCode.DECODER_ERROR));
        // so that encryption is attempted at all
        when(cryptoServiceMock.encryptAes("pass".toCharArray())).thenReturn("encrypted".toCharArray());

        List<ComponentConfigurationImpl> configs = new ArrayList<ComponentConfigurationImpl>();

        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("key1", new Password("pass"));
        cfg.setProperties(props);

        configs.add(cfg);

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        verify(cryptoServiceMock, times(1)).decryptAes("pass".toCharArray());
        verify(cryptoServiceMock, times(1)).encryptAes("pass".toCharArray());

        assertEquals("property was updated", 1, props.size());
        assertTrue("key still exists", props.containsKey("key1"));
        assertArrayEquals("key is encrypted", "encrypted".toCharArray(), ((Password) props.get("key1")).getPassword());
    }

    @Test
    public void testEncryptConfigsPreencryptedPassword() throws Throwable {
        // test encrypting a password when the password is already encrypted
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // decryption succeeds this time
        when(cryptoServiceMock.decryptAes("pass".toCharArray())).thenReturn("pass".toCharArray());

        List<ComponentConfigurationImpl> configs = new ArrayList<ComponentConfigurationImpl>();

        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("key1", new Password("pass"));
        cfg.setProperties(props);

        configs.add(cfg);

        TestUtil.invokePrivate(cs, "encryptConfigs", configs);

        verify(cryptoServiceMock, times(1)).decryptAes("pass".toCharArray());
        verify(cryptoServiceMock, times(0)).encryptAes((char[]) Mockito.anyObject());

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
    public void testGetSnapshots() throws KuraException {
        // test that lower-level method (getSnapshotsInternal) is called; with no/null snapshot directory
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        Set<Long> snapshots = cs.getSnapshots();

        assertNotNull("list is initialized", snapshots);
        assertEquals("list is empty", 0, snapshots.size());

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
    }

    @Test
    public void testGetSnapshotsInternalNullDir() throws Throwable {
        // test with no/null snapshot directory
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        Set<Long> snapshots = (Set<Long>) TestUtil.invokePrivate(cs, "getSnapshotsInternal");

        assertNotNull("list is initialized", snapshots);
        assertEquals("list is empty", 0, snapshots.size());

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
    }

    @Test
    public void testGetSnapshotsInternalNotDir() throws Throwable {
        // test with non-existing snapshot directory
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        when(systemServiceMock.getKuraSnapshotsDirectory()).thenReturn("nonExistingDir");

        Set<Long> snapshots = (Set<Long>) TestUtil.invokePrivate(cs, "getSnapshotsInternal");

        assertNotNull("list is initialized", snapshots);
        assertEquals("list is empty", 0, snapshots.size());

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
    }

    @Test
    public void testGetSnapshotsInternal() throws Throwable {
        // test with existing and full snapshot directory
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        String dir = "existingDir";
        when(systemServiceMock.getKuraSnapshotsDirectory()).thenReturn(dir);

        File d1 = new File(dir);
        d1.mkdirs();

        File f1 = new File(dir, "f1.xml");
        File f2 = new File(dir, "snapshot2.xml");
        File f3 = new File(dir, "snapshot_3.xml");
        File f4 = new File(dir, "snapshot_4_.xml");
        File f5 = new File(dir, "Snapshot_5.XML");

        f1.createNewFile();
        f2.createNewFile();
        f3.createNewFile();
        f4.createNewFile();
        f5.createNewFile();

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        f4.deleteOnExit();
        f5.deleteOnExit();

        Set<Long> snapshots = (Set<Long>) TestUtil.invokePrivate(cs, "getSnapshotsInternal");

        d1.delete();

        assertNotNull("list is initialized", snapshots);
        assertEquals("list has only so many pids", 1, snapshots.size());
        assertEquals("expected pid", 3, (long) snapshots.iterator().next());

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
    }

    /*
     * FIXME: If directory in configuration is null => NPE.
     */
    @Test
    public void testGetSnapshotFileNull() throws Throwable {
        // test if it works with null directory
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        try {
            TestUtil.invokePrivate(cs, "getSnapshotFile", 123);
        } catch (NullPointerException e) {
            // fail("Method result not checked.");
        }

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
    }

    @Test
    public void testGetSnapshotFile() throws Throwable {
        // verify that the file path and name are OK
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        String dir = "someDir";
        when(systemServiceMock.getKuraSnapshotsDirectory()).thenReturn(dir);

        File file = (File) TestUtil.invokePrivate(cs, "getSnapshotFile", 123);

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();

        assertTrue("path pattern matches", file.getAbsolutePath().matches(".*someDir[/\\\\]snapshot_123.xml$"));
    }

    /*
     * FIXME: check for null result
     */
    @Test
    public void testGetSnapshotNullXmlCfgs() throws KuraException {
        // test calling with null configurations list
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            XmlComponentConfigurations loadEncryptedSnapshotFileContent(long snapshotID) throws KuraException {
                XmlComponentConfigurations cfgs = null;
                return cfgs;
            }
        };

        long sid = 0;
        try {
            cs.getSnapshot(sid);
        } catch (Exception e) {
            // fail("Method result not checked.");
        }
    }

    @Test
    public void testGetSnapshotPasswordDecryptionException() throws KuraException {
        // test password decryption failure - log only
        final boolean[] calls = { false, false };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            XmlComponentConfigurations loadEncryptedSnapshotFileContent(long snapshotID) throws KuraException {
                XmlComponentConfigurations cfgs = new XmlComponentConfigurations();
                List<ComponentConfigurationImpl> configurations = new ArrayList<ComponentConfigurationImpl>();
                cfgs.setConfigurations(configurations);

                configurations.add(null);
                ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
                configurations.add(cfg);

                calls[0] = true;

                return cfgs;
            }

            @Override
            Map<String, Object> decryptPasswords(ComponentConfiguration config) {
                calls[1] = true;

                throw new RuntimeException("test");
            }
        };

        long sid = 0;
        List<ComponentConfiguration> configs = null;
        try {
            configs = cs.getSnapshot(sid);
        } catch (Exception e) {
            fail("Exception not expected.");
        }

        assertTrue("config loaded", calls[0]);
        assertTrue("passwords decrypted", calls[1]);
        assertNotNull("configurations list exists", configs);
        assertEquals("configurations list filled", 2, configs.size());
        assertNull(configs.get(0));
        assertNotNull(configs.get(1));
    }

    @Test
    public void testGetSnapshot() throws KuraException {
        // test successful run

        final boolean[] calls = { false, false };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            XmlComponentConfigurations loadEncryptedSnapshotFileContent(long snapshotID) throws KuraException {
                XmlComponentConfigurations cfgs = new XmlComponentConfigurations();
                List<ComponentConfigurationImpl> configurations = new ArrayList<ComponentConfigurationImpl>();
                cfgs.setConfigurations(configurations);

                configurations.add(null);
                ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
                configurations.add(cfg);

                calls[0] = true;

                return cfgs;
            }

            @Override
            Map<String, Object> decryptPasswords(ComponentConfiguration config) {
                calls[1] = true;
                return config.getConfigurationProperties();
            }
        };

        long sid = 0;
        List<ComponentConfiguration> configs = cs.getSnapshot(sid);

        assertTrue("config loaded", calls[0]);
        assertTrue("passwords decrypted", calls[1]);
        assertNotNull("configurations list exists", configs);
        assertEquals("configurations list filled", 2, configs.size());
        assertNull(configs.get(0));
        assertNotNull(configs.get(1));
    }

    @Test
    public void testLoadEncryptedSnapshotFileContentNoFile() throws KuraException {
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        long snapshotID = 123;

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        String dir = "someDir";
        when(systemServiceMock.getKuraSnapshotsDirectory()).thenReturn(dir);

        try {
            cs.loadEncryptedSnapshotFileContent(snapshotID);

            fail("Expected exception: file not found");
        } catch (KuraException e) {
            assertEquals("correct code", KuraErrorCode.CONFIGURATION_SNAPSHOT_NOT_FOUND, e.getCode());
        }

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
    }

    /*
     * FIXME: If crypto service returned null when decrypting, it would crash.
     */
    @Test
    public void testLoadEncryptedSnapshotFileContentNullDecrypt() throws KuraException, IOException {
        // test decryption failure while loading an encrypted snapshot
        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        long snapshotID = 123;

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        String dir = "someDir";
        when(systemServiceMock.getKuraSnapshotsDirectory()).thenReturn(dir);

        File d1 = new File(dir);
        d1.mkdirs();
        d1.deleteOnExit();

        File f1 = new File(dir, "snapshot_" + snapshotID + ".xml");
        f1.createNewFile();
        f1.deleteOnExit();

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        when(cryptoServiceMock.decryptAes((char[]) anyObject())).thenReturn(null);

        try {
            cs.loadEncryptedSnapshotFileContent(snapshotID);
        } catch (NullPointerException e) {
            // fail("Decryption result not checked for null value.");
        }

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();

        f1.delete();
        d1.delete();
    }

    @Test
    public void testLoadEncryptedSnapshotFileContent() throws Exception {
        // load an 'encrypted' snapshot file

        // provide a test configuration
        XmlComponentConfigurations cfgs = new XmlComponentConfigurations();
        List<ComponentConfigurationImpl> cfglist = new ArrayList<ComponentConfigurationImpl>();
        ComponentConfigurationImpl cfg = new ComponentConfigurationImpl();
        cfg.setPid("123");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("pass", "pass");
        cfg.setProperties(props);
        Tocd definition = new Tocd();
        definition.setDescription("description");
        cfg.setDefinition(definition);
        cfglist.add(cfg);
        cfgs.setConfigurations(cfglist);

        StringWriter w = new StringWriter();
        XmlUtil.marshal(cfgs, w);
        String decrypted = w.toString();
        w.close();

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl();

        long snapshotID = 123;

        SystemService systemServiceMock = mock(SystemService.class);
        cs.setSystemService(systemServiceMock);

        String dir = "someDir";
        when(systemServiceMock.getKuraSnapshotsDirectory()).thenReturn(dir);

        File d1 = new File(dir);
        d1.mkdirs();
        d1.deleteOnExit();

        File f1 = new File(dir, "snapshot_" + snapshotID + ".xml");
        f1.createNewFile();
        f1.deleteOnExit();

        FileWriter fw = new FileWriter(f1);
        fw.append("test");
        fw.close();

        CryptoService cryptoServiceMock = mock(CryptoService.class);
        cs.setCryptoService(cryptoServiceMock);

        // ensure the proper file is read
        when(cryptoServiceMock.decryptAes("test".toCharArray())).thenReturn(decrypted.toCharArray());

        XmlComponentConfigurations configurations = cs.loadEncryptedSnapshotFileContent(snapshotID);

        verify(systemServiceMock, times(1)).getKuraSnapshotsDirectory();
        verify(cryptoServiceMock, times(1)).decryptAes("test".toCharArray());

        f1.delete();
        d1.delete();

        assertNotNull("configurations object is returned", configurations);
        assertNotNull("configurations list is returned", configurations.getConfigurations());
        assertEquals("configurations list is not empty", 1, configurations.getConfigurations().size());

        ComponentConfigurationImpl cfg1 = configurations.getConfigurations().get(0);
        assertEquals("correct snapshot", "123", cfg1.getPid());
        assertNotNull("configuration properties map is returned", cfg1.getConfigurationProperties());
        assertEquals("configuration properties map is not empty", 1, cfg1.getConfigurationProperties().size());
    }

    @Test
    public void testLoadLatestSnapshotConfigurationsNullSnapshots() throws Throwable {
        // test null snapshot pids list
        final Set<Long> snapshotList = null;

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            public Set<Long> getSnapshots() throws KuraException {
                return snapshotList;
            }
        };

        List<ComponentConfigurationImpl> result = (List<ComponentConfigurationImpl>) TestUtil.invokePrivate(cs,
                "loadLatestSnapshotConfigurations");

        assertNull("null result", result);
    }

    @Test
    public void testLoadLatestSnapshotConfigurationsEmptySnapshots() throws Throwable {
        // test empty snapshot pids list

        final Set<Long> snapshotList = new TreeSet<Long>();

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            public Set<Long> getSnapshots() throws KuraException {
                return snapshotList;
            }
        };

        List<ComponentConfigurationImpl> result = (List<ComponentConfigurationImpl>) TestUtil.invokePrivate(cs,
                "loadLatestSnapshotConfigurations");

        assertNull("null result", result);
    }

    @Test
    public void testLoadLatestSnapshotConfigurationsNullXML() throws Throwable {
        // test no XML being returned

        final Set<Long> snapshotList = new TreeSet<Long>();
        snapshotList.add(123L);
        snapshotList.add(1234L);

        final boolean[] calls = { false, false };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            public Set<Long> getSnapshots() throws KuraException {
                calls[0] = true;
                return snapshotList;
            }

            @Override
            XmlComponentConfigurations loadEncryptedSnapshotFileContent(long snapshotID) throws KuraException {
                calls[1] = true;

                assertEquals(1234L, snapshotID);

                return null;
            }
        };

        List<ComponentConfigurationImpl> result = (List<ComponentConfigurationImpl>) TestUtil.invokePrivate(cs,
                "loadLatestSnapshotConfigurations");

        assertNull("null result", result);

        assertTrue("call snapshots", calls[0]);
        assertTrue("call load xml", calls[1]);
    }

    @Test
    public void testLoadLatestSnapshotConfigurationsXmlLoads() throws Throwable {
        // test scenario where XML is actually loaded from encrypted file

        final Set<Long> snapshotList = new TreeSet<Long>();
        snapshotList.add(123L);
        snapshotList.add(1234L);

        final XmlComponentConfigurations xmlComponentConfigurations = new XmlComponentConfigurations();
        List<ComponentConfigurationImpl> configurations = new ArrayList<ComponentConfigurationImpl>();
        xmlComponentConfigurations.setConfigurations(configurations);

        final boolean[] calls = { false, false };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            public Set<Long> getSnapshots() throws KuraException {
                calls[0] = true;
                return snapshotList;
            }

            @Override
            XmlComponentConfigurations loadEncryptedSnapshotFileContent(long snapshotID) throws KuraException {
                calls[1] = true;

                assertEquals(1234L, snapshotID);

                return xmlComponentConfigurations;
            }
        };

        List<ComponentConfigurationImpl> result = (List<ComponentConfigurationImpl>) TestUtil.invokePrivate(cs,
                "loadLatestSnapshotConfigurations");

        assertNotNull("xml config not null", result);

        assertTrue("call snapshots", calls[0]);
        assertTrue("call load xml", calls[1]);
    }

    @Test
    public void testLoadLatestSnapshotConfigurationsRecursiveAfterEncryption() throws Throwable {
        // test scenario where latest snapshot is not encrypted and all snapshots are encrypted before being loaded

        final Set<Long> snapshotList = new TreeSet<Long>();
        snapshotList.add(123L);
        snapshotList.add(1234L);

        final XmlComponentConfigurations xmlComponentConfigurations = new XmlComponentConfigurations();
        List<ComponentConfigurationImpl> configurations = new ArrayList<ComponentConfigurationImpl>();
        xmlComponentConfigurations.setConfigurations(configurations);

        final String dir = "snapDir";
        File d1 = new File(dir);
        d1.mkdirs();
        d1.deleteOnExit();

        File f1 = new File(d1, "snapshot_123.xml");
        f1.createNewFile();
        f1.deleteOnExit();
        File f2 = new File(d1, "snapshot_1234.xml");
        f2.createNewFile();
        f2.deleteOnExit();

        final int[] calls = { 0, 0 };

        ConfigurationServiceImpl cs = new ConfigurationServiceImpl() {

            @Override
            public Set<Long> getSnapshots() throws KuraException {
                calls[0]++;
                if (calls[0] < 3) {
                    return snapshotList;
                } else {
                    return null;
                }
            }

            @Override
            XmlComponentConfigurations loadEncryptedSnapshotFileContent(long snapshotID) throws KuraException {
                calls[1]++;
                throw new KuraException(KuraErrorCode.CONFIGURATION_ERROR);
            }
        };

        List<ComponentConfigurationImpl> result = (List<ComponentConfigurationImpl>) TestUtil.invokePrivate(cs,
                "loadLatestSnapshotConfigurations");

        assertNull("xml config null", result);

        assertEquals("call snapshots", 4, calls[0]);
        assertEquals("call load xml", 3, calls[1]);
    }

    @Test
    public void testEncryptPlainSnapshots() {
        // TODO
    }

    @Test
    public void testWriteSnapshot() {
        // TODO
    }

    @Test
    public void testSaveSnapshot() {
        // TODO
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
