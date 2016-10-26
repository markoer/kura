package org.eclipse.kura.core;

import java.lang.reflect.Field;

public class TestUtil {
	public static Object getFieldValue(Object svc, String fieldName) {
		Object result = null;
		Field field = null;
		Class clazz = svc.getClass();
		while (!(clazz == Object.class || field != null)) {
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
				continue;
			}

			try {
				field.setAccessible(true);
				result = field.get(svc);

				break;
			} catch (IllegalArgumentException e) {
				// TODO
			} catch (IllegalAccessException e) {
				// TODO
			}
		}

		return result;
	}
}
