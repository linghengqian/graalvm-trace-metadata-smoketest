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

    public void selectTeachersAndStudents() {
        teacherService.selectTeachers();
        studentService.selectStudents();
    }

    public void selectTeachersInnerStudents() {
        teacherService.selectTeachersInnerStudents();
    }

    public void addTeacherAndStudent() {
        teacherService.addTeacherWithTx("ss", 1);
        studentService.addStudentWithTx("tt", 2);
    }

    @Transactional
    public void addTeacherAndStudentWithTx() {
        teacherService.addTeacherWithTx("ss", 1);
        studentService.addStudentNoTx("tt", 2);
    }
}
