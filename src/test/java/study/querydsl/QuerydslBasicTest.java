package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);
        Member member5 = new Member("member5", 50, teamB);
        Member member6 = new Member("member6", 60, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
    }

    @Test
    void startQuerydsl() {
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertNotNull(findMember);
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void findAll() {
        // which

        //when
        QMember m = new QMember("m");
        List<Member> findList = queryFactory
                .select(m)
                .from(m)
                .fetch();

        //then
        assertThat(findList.size()).isEqualTo(6);

        for (Member member : findList) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void startQuerydslStaticImport() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertNotNull(findMember);
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void queryPrint() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(
//                        member.username.eq("member1")             // username = "member1"
//                        member.username.ne("member1")             // username != "member1"
//                        member.username.eq("member1").not()       // username != "member1"
//                        member.username.isNotNull()
//                        member.age.in(10, 20)
//                        member.age.notIn(10, 20)
//                        member.age.between(10, 40)
//                        member.age.goe(20)
//                        member.age.gt(20)
//                        member.age.loe(20)
                        member.age.lt(20)


                )
                .fetch();
        assertNotNull(members);

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    void simpleFetchTest() {
        // List
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOneMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Member fetchFirstMember = queryFactory
                .selectFrom(QMember.member)
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        List<Member> getMembers = results.getResults();
        for (Member getMember : getMembers) {
            System.out.println("getMember = " + getMember);
        }
        assertNotNull(getMembers);
        assertThat(getMembers.size()).isEqualTo(6);
        System.out.println("results.getLimits = " + results.getLimit());
        System.out.println("results.getOffset() = " + results.getOffset());
        System.out.println("results.getTotal() = " + results.getTotal());
    }

    /**
     * 회원 정렬 순서.
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
     */
    @Test
    void sort() {
        em.persist(new Member(null, 90));
        em.persist(new Member("member7", 90));
        em.persist(new Member("member8", 90));

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(90))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }

        assertThat(members.size()).isEqualTo(3);
    }

    @Test
    void paging1() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)                          // 0부터 시작(zero index)
                .limit(4)                           // 최대 개수 조회.
                .fetch();

        assertThat(members.size()).isEqualTo(4);
    }

    @Test
    void paging2() {
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)
                .limit(4)
                .fetchResults();

        List<Member> members = results.getResults();
        assertThat(members.size()).isEqualTo(4);
        assertThat(results.getTotal()).isEqualTo(6);
        assertThat(results.getOffset()).isZero();
        assertThat(results.getLimit()).isEqualTo(4);
    }

    @Test
    void aggregation() {
        List<Tuple> fetch = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.min(),
                        member.age.max(),
                        member.age.avg())
                .from(member)
                .fetch();

        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(6);
        assertThat(tuple.get(member.age.sum())).isEqualTo(210);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(60);
        assertThat(tuple.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void groupBy() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())    // 팀 이름별 GroupBy, Name, 팀 평균 나이.
                .from(member)
                .join(member.team, team)                // Member.팀 팀 JOIN
                .groupBy(team.name)                     // 어떤걸루 그룹핑할지 정리.
                .fetch();

        assertNotNull(result);
        assertThat(result.size()).isEqualTo(2);         // 두개의 팀으로 분리.
    }

    @Test
    void join() {
        List<Member> teamA = queryFactory
                .selectFrom(member)
                .join(member.team, team)                // join, innerJoin 내부조인.
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA)
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }
}
