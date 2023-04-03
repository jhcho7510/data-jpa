package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.dto.UsernameOnlyDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // JunitTest에서 테스트 종료후 기본 Rollback을 한다. Rollback을 하지 않으려면 아래처럼 기술한다. @Rollback(false)
@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());

        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성 보장

    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 =
                memberRepository.findById(member1.getId()).get();
        Member findMember2 =
                memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result =
                memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result = memberRepository.findByUsername("AAA");

        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void testFindUser() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA",10);
        assertThat(result.get(0)).isEqualTo(m1);

    }

    @Test
    public void testFindUsername() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        usernameList.stream().forEach(System.out::println);

    }

    @Test
    public void testFindMemberDto() {
        Team t1 = new Team("team!");
        teamRepository.save(t1);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(t1);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        memberDto.stream().forEach(System.out::println);

    }

    @Test
    public void testFindByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> usernameList = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        usernameList.stream().forEach(System.out::println);

    }


    @Test
    public void paging() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,"username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest);
//        Slice<Member> page = memberRepository.findSliceByAge(10, pageRequest);

        // 엔티티를 직접 반환하지 말고, Dto타입으로 변경하여, 반환을 하도록 해야한다.
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();
        System.out.println("totalElements = " + totalElements);


        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?



    }

    @Test
    public void bulkUpdate() throws Exception {
        /** Bulk연산 이후에는 영속성 컨텍스트를 제거해야, 조회시 예상값 획득 가능, 미제거시 DB데이터와 영속성 컨텍스트 결과값 불일치함. */
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));
        //when
        int resultCount = memberRepository.bulkAgePlus(20);
//        entityManager.clear();
        
        List<Member> result = memberRepository.findByUsername("member5");
        Member member5  = result.get(0);
        System.out.println("member5 = " + member5);


        //then
        assertThat(resultCount).isEqualTo(3);
    }

    /** @EntityGraph
     *   . FETCH JOIN : 연관관계 있는 테이블들의 데이터를 한번에 모두 가져온다.
     *   . FETCH JOIN하면 기본적으로 LEFT OUTER JOIN으로 실행된다.
     */
    @Test
    public void findMemberLazy() throws Exception {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));
        entityManager.flush();
        entityManager.clear();

//        List<Member> members = memberRepository.findAll();
//        List<Member> members = memberRepository.findMemberFetchJoin();
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");


        members.stream().forEach(member -> {
            System.out.println("member's team name = " + member.getTeam().getName());
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        });

    }

    @Test
    public void queryHint() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        entityManager.flush();
        entityManager.clear();
        //when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");
        entityManager.flush(); //Update Query 실행X
    }

    @Test
    public void lock() {
        Member member = new Member("member1",10);
        List<Member> result = memberRepository.findLockByUsername("member1");
    }

    @Test
    public void customRepositoryTest() {
        /** 사용자정의 Repository에 정의한 메서드를 호출한다. memberRepository 인터페이스는 MemberCustomRepository를 상속받았기때문에 아래처럼 사용할 수 있음*/
        List<Member> customMember = memberRepository.findCustomMember();
    }


    @Test
    public void projections() throws Exception {
        //given
        Team teamA = new Team("teamA");
        entityManager.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        entityManager.persist(m1);
        entityManager.persist(m2);
        entityManager.flush();
        entityManager.clear();
        //when
        List<UsernameOnly> result =
                memberRepository.findProjectionsByUsername("m1");
        result.stream().forEach(System.out::println);
        //then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void projectionsDto() throws Exception {
        //given
        Team teamA = new Team("teamA");
        entityManager.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        entityManager.persist(m1);
        entityManager.persist(m2);
        entityManager.flush();
        entityManager.clear();
        //when
        List<UsernameOnlyDto> result =
                memberRepository.findProjectionsByUsername("m1", UsernameOnlyDto.class);
//        for(UsernameOnlyDto usernameOnlyDto: result) {
//            System.out.println("usernameOnlyDto.getUsername() = " + usernameOnlyDto.getUsername());
//        }
        //then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    
    
    @Test
    public void nativeQuery() {
        Team teamA = new Team("teamA");
        entityManager.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        entityManager.persist(m1);
        entityManager.persist(m2);
        entityManager.flush();
        entityManager.clear();
        
//        Member result = memberRepository.findByNativeQuery("m1");
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0,10));
        result.getContent().stream().forEach(content -> {
            System.out.println("content.getUsername() = " + content.getUsername());
            System.out.println("content.getTeamName() = " + content.getTeamName());
        });
//        System.out.println("result = " + result);
    }
    

}