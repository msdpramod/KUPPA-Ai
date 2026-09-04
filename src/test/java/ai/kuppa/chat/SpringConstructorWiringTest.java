package ai.kuppa.chat;

import ai.kuppa.audit.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringConstructorWiringTest {

    @Test
    void multiConstructorServicesExplicitlyDeclareSpringInjectionConstructor() throws Exception {
        assertAutowired(ContinuitySessionService.class.getConstructor(String.class, long.class));
        assertAutowired(OwnerDeviceIdentityService.class.getConstructor(
                String.class, String.class, String.class, String.class, long.class));
        assertAutowired(OwnerDeviceTrustService.class.getConstructor(
                OwnerDeviceTrustRepository.class, AuditService.class));
    }

    private void assertAutowired(Constructor<?> constructor) {
        assertTrue(constructor.isAnnotationPresent(Autowired.class),
                () -> constructor.getDeclaringClass().getSimpleName()
                        + " must explicitly select its Spring injection constructor");
    }
}
