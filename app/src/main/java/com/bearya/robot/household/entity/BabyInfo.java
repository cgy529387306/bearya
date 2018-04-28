package com.bearya.robot.household.entity;

/**
 * Created by caigy on 2018/4/20.
 */

public class BabyInfo {
    private int baby_id;
    private String name;
    private int gender;
    private long birthday;
    private int age;
    private String avatar;
    private int is_default;
    private String relationship;
//    private List<Integer> tags;

    public int getBaby_id() {
        return baby_id;
    }

    public void setBaby_id(int baby_id) {
        this.baby_id = baby_id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAvatar() {
        return avatar == null ? "" : avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getIs_default() {
        return is_default;
    }

    public void setIs_default(int is_default) {
        this.is_default = is_default;
    }

    public String getRelationship() {
        return relationship == null ? "" : relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

//    public List<Integer> getTags() {
//        if (tags == null) {
//            return new ArrayList<>();
//        }
//        return tags;
//    }
//
//    public void setTags(List<Integer> tags) {
//        this.tags = tags;
//    }
}
