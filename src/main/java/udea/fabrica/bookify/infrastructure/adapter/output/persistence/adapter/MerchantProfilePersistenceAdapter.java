package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.MerchantProfileEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.MerchantProfileSpringRepository;

import java.util.Optional;

@Component
public class MerchantProfilePersistenceAdapter implements MerchantProfileRepositoryPort {

    private final MerchantProfileSpringRepository repository;

    public MerchantProfilePersistenceAdapter(MerchantProfileSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public MerchantProfile save(MerchantProfile profile) {
        MerchantProfileEntity entity = MerchantProfileEntity.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .businessName(profile.getBusinessName())
                .category(profile.getCategory())
                .address(profile.getAddress())
                .phone(profile.getPhone())
                .build();

        MerchantProfileEntity saved = repository.save(entity);
        profile.setId(saved.getId());
        return profile;
    }

    @Override
    public Optional<MerchantProfile> findByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(e -> MerchantProfile.builder()
                        .id(e.getId())
                        .userId(e.getUserId())
                        .businessName(e.getBusinessName())
                        .category(e.getCategory())
                        .address(e.getAddress())
                        .phone(e.getPhone())
                        .build());
    }
}