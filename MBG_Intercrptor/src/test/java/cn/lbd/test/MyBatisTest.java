package cn.lbd.test;

import cn.lbd.dao.UserMapper;
import cn.lbd.domain.User;
import cn.lbd.domain.UserExample;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MyBatisTest {
    private InputStream is;
    private SqlSession sqlSession;
    private UserMapper mapper;
    @Before
    public void init() throws IOException {
        is=Resources.getResourceAsStream("SqlMapConfig.xml");
        SqlSessionFactoryBuilder builder=new SqlSessionFactoryBuilder();
        SqlSessionFactory factory=builder.build(is);
        sqlSession=factory.openSession();
        mapper=sqlSession.getMapper(UserMapper.class);
    }
    @After
    public void destroy() throws IOException {
        sqlSession.commit();
        sqlSession.close();
        is.close();
    }

    @Test
    public void test(){
        //xxxExample就是封装查询条件的
        //封装用户查询条件的example
        UserExample userExample=new UserExample();
        //创建一个Criteria，这个Criteria就是拼装查询条件，设置完查询条件会封装到UserExample中
        UserExample.Criteria criteria=userExample.createCriteria();
        //查询用户名带张的，且地址在陕西的，或者名字为李六的
        criteria.andNameLike("%张%");
        criteria.andAddressEqualTo("陕西");

        UserExample.Criteria criteria1=userExample.createCriteria();
        criteria1.andNameEqualTo("李六");
        //将criteria1中的查询条件以OR为连接符拼装到criteria条件后面;
        userExample.or(criteria1);

        /*
        Preparing: select id, email, name, gender, age, address, qq, username, password from user
        WHERE ( name like ? and address = ? ) or( name = ? )

        Parameters: %张%(String), 陕西(String), 李六(String)
        */
        List<User> users = mapper.selectByExample(userExample);
        for (User user : users) {
            System.out.println(user);
        }
    }
    @Test
    public void testfindALL(){
        PageHelper.startPage(4, 5);
        List<User> users = mapper.findAll();
        PageInfo<User> page = new PageInfo<User>(users,5);
        for (User user : users) {
            System.out.println(user);
        }
        System.out.println("当前页码："+page.getPageNum());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("每页记录数："+page.getPageSize());
        System.out.println("总页码："+page.getPages());
        System.out.println("是否第一页："+page.isIsFirstPage());
        int[] navigatepageNums = page.getNavigatepageNums();
        for (int navigatepageNum : navigatepageNums) {
            System.out.println(navigatepageNum);
        }
    }

    @Test
    public void testfindById(){
        User user = mapper.findById(10);
        System.out.println(user);
    }


}
