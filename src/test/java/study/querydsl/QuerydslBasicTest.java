package study.querydsl;

import com.querydsl.core.QueryResults;
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
}
