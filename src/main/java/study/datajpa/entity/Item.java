package study.datajpa.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
public class Item implements Persistable<String> {
    @Id
    private String id;
    @CreatedDate
    private LocalDateTime createdDate;
    public Item(String id) {
        this.id = id;
    }
    @Override
    public String getId() {
        return id; }
    @Override
    public boolean isNew() {
        return createdDate == null;
    }

}
