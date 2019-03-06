package grader.frontend;

import grader.backend.Student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/*
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   2/6/19
 * @version 1.0.0
 */
public enum SortOrder {
    LAST_NAME_ASC("in ascending order by last name"),
    LAST_NAME_DESC("in descending order by last name"),
    FIRST_NAME_ASC("in ascending order by first name"),
    FIRST_NAME_DESC("in descending order by first name"),
    GRADE_ASC("in ascending order by grade"),
    GRADE_DESC("in descending order by grade");

    String description;

    SortOrder(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

    public ArrayList<Student> sort(Collection<Student> input) {
        ArrayList<Student> result = new ArrayList<>(input);

        result.sort((student1, student2) -> {
            if (student1.equals(student2)) {
                return 0;
            }

            switch (this) {
                default:
                case LAST_NAME_ASC:
                    return student1.getLastName().compareTo(student2.getLastName());

                case LAST_NAME_DESC:
                    return student2.getLastName().compareTo(student1.getLastName());


                case FIRST_NAME_ASC:
                    return student1.getName().compareTo(student2.getName());

                case FIRST_NAME_DESC:
                    return student2.getName().compareTo(student1.getName());

                case GRADE_ASC:
                    if (student1.getFeedback() == null) {
                        return student2.getFeedback() == null ? 0 : 1;
                    } else if (student2.getFeedback() == null) {
                        return -1;
                    }

                    return (int) (student1.getFeedback().getGrade()
                            - student2.getFeedback().getGrade());

                case GRADE_DESC:
                    if (student1.getFeedback() == null) {
                        return student2.getFeedback() == null ? 0 : 1;
                    } else if (student2.getFeedback() == null) {
                        return -1;
                    }

                    return (int) (student2.getFeedback().getGrade()
                                      - student1.getFeedback().getGrade());
            }
        });

        return result;
    }

    public SortOrder reverse() {
        switch (this) {
            case LAST_NAME_ASC:   return LAST_NAME_DESC;
            case LAST_NAME_DESC:  return LAST_NAME_ASC;
            case FIRST_NAME_ASC:  return FIRST_NAME_DESC;
            case FIRST_NAME_DESC: return FIRST_NAME_ASC;
            case GRADE_ASC:       return GRADE_DESC;

            default:
            case GRADE_DESC:      return GRADE_ASC;
        }
    }

}
