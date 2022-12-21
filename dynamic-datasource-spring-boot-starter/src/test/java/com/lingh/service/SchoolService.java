package com.lingh.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolService {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;

    @Transactional
    public int addTeacherAndStudentWithTx() {
        int aa = teacherService.addTeacherNoTx("aa", 3);
        int bb = studentService.addStudentNoTx("bb", 4);
        return aa + bb;
    }
}
