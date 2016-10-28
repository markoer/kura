package org.eclipse.kura.core.testutil;

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
			    e.printStackTrace();
			} catch (IllegalAccessException e) {
			    e.printStackTrace();
			}
		}

		return result;
	}

	private static Field getField(Object svc, String fieldName) throws NoSuchFieldException {
	    Field field = null;
	    Class clazz = svc.getClass();
	    while (!(clazz == Object.class || field != null)) {
	        try {
	            field = clazz.getDeclaredField(fieldName);
	            break;
	        } catch (NoSuchFieldException e) {
	        }
	        clazz = clazz.getSuperclass();
	    }

	    if (field == null) {
	        throw new NoSuchFieldException("Field not found");
	    }

	    return field;
	}

    public static void setFieldValue(Object svc, String fieldName, Object value) throws NoSuchFieldException {
        Field field = getField(svc, fieldName);

        field.setAccessible(true);

        try {
            field.set(svc, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
