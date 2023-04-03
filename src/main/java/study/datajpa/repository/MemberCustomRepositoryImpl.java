package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

import java.util.List;

@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository{

    private final EntityManager entityManager;

//    public MemberCustomRepositoryImpl(EntityManager entityManager) {
//        this.entityManager = entityManager;
//    }

    @Override
    public List<Member> findCustomMember() {
        return entityManager.createQuery("select m from Member m")
                .getResultList();
    }
}
