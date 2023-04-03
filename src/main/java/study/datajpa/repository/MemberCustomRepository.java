package study.datajpa.repository;

import study.datajpa.entity.Member;

import java.util.List;

/** 사용자정의 Repository 구현 */
public interface MemberCustomRepository {
    public List<Member> findCustomMember();

}
