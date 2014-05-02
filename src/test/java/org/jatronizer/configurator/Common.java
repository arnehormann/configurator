package org.jatronizer.configurator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.Assert.fail;

public final class Common {
	public static interface Action {
		void run(Object[] args);
	}

	public static interface Call {
		Call on(Object base);
		Object with(Object...args);
	}

	private static class ACall implements Call {
		private final Method method;
		private Object base;
		private boolean isStatic;

		public ACall(Object base, String name, Class...argTypes) {
			try {
				if (base instanceof Class) {
					this.method = ((Class) base).getMethod(name, argTypes);
				} else {
					this.method = base.getClass().getMethod(name, argTypes);
				}
				this.isStatic = (this.method.getModifiers() & Modifier.STATIC) != 0;
				this.base = this.isStatic ? null : base;
			} catch (Exception e) {
				throw new RuntimeException("Call(...) failed", e);
			}
		}

		public Call on(Object base) {
			if (isStatic) {
				throw new RuntimeException("on(...) failed, method is static");
			} else if (base == null) {
				throw new RuntimeException("on(null) failed, method is not static");
			}
			if (base.getClass() != method.getDeclaringClass()) {
				throw new RuntimeException("on(...) failed: not an instance of " + method.getDeclaringClass());
			}
			this.base = base;
			return this;
		}

		public Object with(Object... args) {
			if (args == null) {
				// with varargs, this happens when null is the only argument
				args = new Object[]{null};
			}
			try {
				this.method.invoke(base, args);
				return this;
			} catch (Exception e) {
				throw new RuntimeException("with(...) failed", e);
			}
		}

		public String toString() {
			return method.toString();
		}
	}

	public static Action FAIL_ON_CALLS = new Action() {
		public void run(Object[] args) {
			Call call = (Call) args[0];
			Object[] callArgs = new Object[args.length - 1];
			System.arraycopy(args, 1, callArgs, 0, callArgs.length);
			try {
				Object o = call.with(callArgs);
				fail("Expected an Exception on calling " + call + " with " + Arrays.toString(callArgs));
			} catch (Exception e) {
			}
		}
	};

	public static void each(Object[][] scenarios, Action... actions) {
		for (Object[] scenario : scenarios) {
			for (Action action : actions) {
				action.run(scenario);
			}
		}
	}

	public static Call call(Object base, String name, Class...argTypes) {
		return new ACall(base, name, argTypes);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getConstant(Class type, Class<T> valueType, String name) {
		Field field;
		Object value;
		try {
			field = type.getDeclaredField(name);
			value = field.get(null);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		final int staticFinal = Modifier.FINAL | Modifier.STATIC;
		if ((field.getModifiers() & staticFinal) != staticFinal) {
			throw new RuntimeException(field + " is not static final");
		}
		if (valueType != null && value.getClass() != valueType) {
			throw new RuntimeException(name + " is not a " + valueType.getSimpleName());
		}
		return (T) value;
	}
}
