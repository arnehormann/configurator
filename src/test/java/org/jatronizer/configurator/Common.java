package org.jatronizer.configurator;

import org.junit.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.Assert.fail;

public class Common {
	public static interface Action {
		void run(Object[] args);
	}

	public static final Action HAS_IDENTICAL_ELEMENTS = new Action() {
		public void run(Object[] args) {
			if (args.length < 2) {
				throw new RuntimeException("at least two arguments required, got " + Arrays.toString(args));
			}
			Object reference = args[0];
			for (int i = 1; i < args.length; i++) {
				Assert.assertSame(reference, args[i]);
			}
		}
	};

	public static final Action HAS_EQUAL_ELEMENTS = new Action() {
		public void run(Object[] args) {
			if (args.length < 2) {
				throw new RuntimeException("at least two arguments required, got " + Arrays.toString(args));
			}
			Object reference = args[0];
			for (int i = 1; i < args.length; i++) {
				Assert.assertEquals(reference, args[i]);
			}
		}
	};

	public static void eachRow(Object[][] scenarios, Action... actions) {
		for (Object[] scenario : scenarios) {
			for (Action action : actions) {
				action.run(scenario);
			}
		}
	}

	public static <T> T[] Range(int offset, int width, T...data) {
		return Arrays.copyOfRange(data, offset, offset + width);
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

	public static interface Call {
		Call on(Object base);
		Object with(Object...args);
	}

	public static Call call(Object base, String name, Class...argTypes) {
		return new ACall(base, name, argTypes);
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
}
