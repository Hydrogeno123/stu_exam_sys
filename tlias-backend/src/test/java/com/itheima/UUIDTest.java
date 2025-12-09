package com.itheima;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

public class UUIDTest {

    @Test
    public void testUuid(){
        for (int i = 0; i < 10; i++) {
            System.out.println(UUID.randomUUID().toString());
        }
    }

    @Test
    public void testHashSet(){
        HashSet<User> users = new HashSet<User>();
        users.add(new User("aaa", 18));
        users.add(new User("bbb", 19));
        users.add(new User("ccc", 20));
        users.add(new User("aaa", 18));

        for (User user : users) {
            System.out.println(user.getName() + " : " + user.getAge());
        }
    }

}
