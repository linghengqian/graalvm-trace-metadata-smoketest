package com.lingh;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.lingh.service.SchoolService;
import com.lingh.service.StudentService;
import com.lingh.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DynamicDatasourceApplication.class)
public class NestDataSourceTest {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolService schoolService;

    @Test
    void testNest() {
        DataSourceProperty masterDataSourceProperty = new DataSourceProperty()
                .setPoolName("master").setDriverClassName("org.h2.Driver")
                .setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/schema.sql'")
                .setUsername("sa").setPassword("");
        DataSourceProperty salveDataSourceProperty = new DataSourceProperty()
                .setPoolName("salve").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty teacherDataSourceProperty = new DataSourceProperty()
                .setPoolName("teacher").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty studentDataSourceProperty = new DataSourceProperty()
                .setPoolName("student").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource("master", dataSourceCreator.createDataSource(masterDataSourceProperty));
        ds.addDataSource("salve", dataSourceCreator.createDataSource(salveDataSourceProperty));
        ds.addDataSource("teacher", dataSourceCreator.createDataSource(teacherDataSourceProperty));
        ds.addDataSource("student", dataSourceCreator.createDataSource(studentDataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("master", "salve", "teacher", "student");
        schoolService.addTeacherAndStudent();
        assertThat(teacherService.selectTeachers()).isEqualTo(List.of(new Teacher(1, "tt", 2)));
        assertThat(studentService.selectStudents()).isEqualTo(List.of(new Student(1, "tt", 2)));
        schoolService.selectTeachersAndStudents(); //nest2
        schoolService.selectTeachersInnerStudents(); //nest3
        schoolService.addTeacherAndStudentWithTx();
    }
}
