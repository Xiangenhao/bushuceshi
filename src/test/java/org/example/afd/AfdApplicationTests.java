package org.example.afd;

import org.example.afd.mapper.PostMapper;
import org.example.afd.mapper.UserMapper;
import org.example.afd.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AfdApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;

    @Test
    void getZones() {


    }

}
