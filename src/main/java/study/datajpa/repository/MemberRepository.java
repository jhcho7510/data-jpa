package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.dto.UsernameOnlyDto;
import study.datajpa.entity.Member;


import java.util.Collection;
import java.util.List;

/** MemberCustomRepository는 사용자 정의 Repository인데, MemberRepository에서 상속 받는 것 외에 별도의 Repository를 생성해서 작업하는것을 추천 */
public interface MemberRepository extends JpaRepository<Member,Long>, MemberCustomRepository {
//    List<Member> findByUsername(String username);

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query(name = "Member.findByUsername") // spring data NamedQuery..
    List<Member> findByUsername(@Param("username") String username); // Jpql에 :xxx 형태로 파라미터가 있을때는 @Param(...) 형태로 사용

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    /** DTO 유형의 반환타입 */
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) " +
            "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m left join m.team t", countQuery = "select count(m) from Member m")
    Page<Member> findExtractCountByAge(int age, Pageable pageable);

    // Bulk성 Update - 단건이 아닌 다수건의 내용을 수정할때 사용.
    @Modifying(clearAutomatically = true) // Bulk연산 이후 EntityManager.clear()메서드를 자동으로 실행해서, 별도로 영속성 컨텍스트 제거를 하지 않아도 됨.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param(value = "age") int age);

    /** Fetch Join : Member와 Team의 데이터를 조회시 한번에 가져오려는 행위를 Fetch join 이라고 한다. (각각 Member, Team으로 접근하는것이 아님)*/
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();



    // JPQL의 Fetch join을 위해서 @EntityGraph를 사용했었는데, @Query와 복합사용 가능하다.
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // Username을 조건으로 Member와 Team을 Fetch join 한다.
    @EntityGraph(attributePaths = ("team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    /** 성능 최적화를 위해 QueryHints를 사용하기는 하나, 모든 곳에 적용할 만큼 효용성은 없다. */
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value ="true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);

    /**projection sample */
    List<UsernameOnly> findProjectionsByUsername(String username);

    <T> List<T> findProjectionsByUsername(String username, Class<T> type);

    @Query(value = "select * from member where username = ?", nativeQuery =true)
    Member findByNativeQuery(String username);

    @Query(
            value = "select m.member_id as id, m.username, t.name as teamName from member m left join team t",
            countQuery = "select count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
