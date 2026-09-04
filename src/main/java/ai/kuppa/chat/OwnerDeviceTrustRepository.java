package ai.kuppa.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnerDeviceTrustRepository extends JpaRepository<OwnerDeviceTrust, String> {
    List<OwnerDeviceTrust> findByOwnerIdOrderByEnrolledAtDesc(String ownerId);
}
