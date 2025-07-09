
@Insert("INSERT INTO user (user_id, username, password, email, phone_number, avatar, gender, birthday, signature, region, registration_time, last_login_time) " +
        "VALUES (#{userId}, #{username}, #{password}, #{email}, #{phoneNumber}, #{avatar}, #{gender}, #{birthday}, #{signature}, #{region}, #{registrationTime}, #{lastLoginTime})")
public void insertUser(User user);
