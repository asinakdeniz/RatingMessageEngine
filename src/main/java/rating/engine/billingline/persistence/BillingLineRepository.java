package rating.engine.billingline.persistence;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingLineRepository extends CrudRepository<BillingLineEntity, Long> {

    // In repository
    @Query(value = """
            SELECT * FROM billing_line 
            WHERE status = :status 
            LIMIT :limit 
            FOR UPDATE SKIP LOCKED
            """)
    List<BillingLineEntity> findAndLockUnprocessed(BillingLineStatus status, int limit);

    @Query("SELECT * FROM rating_engine.billing_line WHERE status = :status ORDER BY id LIMIT :limit")
    List<BillingLineEntity> findByStatusWithLimit(BillingLineStatus status, int limit);

    @Modifying
    @Query("UPDATE rating_engine.billing_line SET status = :status WHERE id IN (:ids)")
    int updateStatusByIds(BillingLineStatus status, List<Long> ids);

    @Modifying
    @Query("UPDATE rating_engine.billing_line SET status = :status WHERE id = ANY(:ids)")
    int updateStatusByIds(BillingLineStatus status, Long[] ids);
}
