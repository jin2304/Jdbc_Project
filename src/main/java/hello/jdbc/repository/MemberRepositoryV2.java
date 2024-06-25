package hello.jdbc.repository;



import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    //dataSource 주입받음
    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }



    //회원 저장
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch(SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            //리소스 정리, 자원을 다 쓰고 직접 닫아줘야 함
            close(con, pstmt, null);
        }

    }


    //회원 조회
    public Member findById(String memberId) throws SQLException{
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }


        }catch (SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }


    }


    //회원 수정
    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2,  memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch(SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            //리소스 정리, 자원을 다 쓰고 직접 닫아줘야 함
            close(con, pstmt, null);
        }

    }



    //회원 삭제
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch(SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            //리소스 정리, 자원을 다 쓰고 직접 닫아줘야 함
            close(con, pstmt, null);
        }

    }




    //회원 조회v2 (커넥션을 유지하기 위해서 서비스 계층으로부터 커넥션을 받아서 사용)
    public Member findById(Connection con, String memberId) throws SQLException{
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }


        }catch (SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            //connection은 여기서 닫지 않음
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
            //JdbcUtils.closeConnection(); x
        }


    }


    //회원 수정v2
    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2,  memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch(SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            //connection은 여기서 닫지 않음
            JdbcUtils.closeStatement(pstmt);
            //JdbcUtils.closeConnection(); x
        }

    }

    private void close(Connection con, Statement stmt, ResultSet rs){
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {

        //dataSource 라는 추상화에 의존하는 코드
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
        //return DBConnectionUtil.getConnection();
    }
}