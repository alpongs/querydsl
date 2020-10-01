package study.querydsl.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@SpringBootTest
@Transactional
@Commit
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);
        Member member5 = new Member("member5", 50, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);

        em.flush();
        em.clear();

        List<Member> fromMembers = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member fromMember : fromMembers) {
            System.out.println("fromMember = " + fromMember);
            System.out.println("fromMember.getTeam() = " + fromMember.getTeam());   // 지연 로딩 동작 확인용.
        }

        Assertions.assertThat(fromMembers.size()).isEqualTo(5);
    }
}