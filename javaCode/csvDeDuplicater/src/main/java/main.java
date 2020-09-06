import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import static java.lang.System.exit;


public class main {
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to DE-Duplicator");
        if (args.length != 2) {
            System.out.println("Usage: arg 1 = input file, arg 2 = output file");
            exit(0);
        }
        String csvInFile = args[0];
        String csvOutFile = args[1];
        String line = "";
        StudentBody studentBody = new StudentBody();
        boolean firstLine = true;

        try (BufferedReader csvBufferedReader = new BufferedReader(new FileReader(csvInFile))) {
            while ((line = csvBufferedReader.readLine()) != null) {

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                Student studentToParse = new Student(line.split(","));

                if (studentBody.isStudentInSystem(studentToParse)) {
                    //student exists at one or more locations
                    System.out.println("possible duplicate student entry: " + studentToParse.studentName + " ID: " + studentToParse.studentSisID);
                    studentBody.updateStudentInHash(studentToParse);
                } else {
                    System.out.println("New student: " + studentToParse.studentName);
                    studentBody.addStudent((studentToParse));
                }
            }

        }

        System.out.println("Number of unique students: " + studentBody.studentsInSystem.keySet().size());

        try (BufferedWriter csvOutput = new BufferedWriter(new FileWriter(csvOutFile))) {
            csvOutput.write("student_name,student_sis_id,student_grade,student_language,parent1_name,parent1_email,parent1_phone,parent1_language,parent2_name,parent2_email,parent2_phone,parent2_language,class_name,class_sis_id\n");

            //For each unique student, sent the list of that students classes to be written out
            for (Map.Entry<Integer, ArrayList<Student>> studentToWrite : studentBody.studentsInSystem.entrySet()) {
                Student.writeOutStudent(studentToWrite.getValue(), csvOutput);
            }
        }
    }


    public static class StudentBody {
        HashMap<Integer, ArrayList<Student>> studentsInSystem;
        HashMap<Integer, ArrayList<Student>> problematicEntries;


        public StudentBody() {
            studentsInSystem = new HashMap<>();
            problematicEntries = new HashMap<>();
        }

        public void addStudent(Student studentToAdd) {
            ArrayList<Student> totalListOfClasses = new ArrayList<>();
            totalListOfClasses.add(studentToAdd);
            studentsInSystem.put(Integer.parseInt(studentToAdd.studentSisID), totalListOfClasses);
        }

        /**
         * Is this a new student
         *
         * @param studentObject
         * @return
         */
        public boolean isStudentInSystem(Student studentObject) {
            return (studentsInSystem.containsKey(Integer.parseInt(studentObject.studentSisID)));
        }

        public void updateStudentInHash(Student studentToParse) {
            ArrayList<Student> studentToUpdateClassList = studentsInSystem.get(Integer.parseInt(studentToParse.studentSisID));
            int x = 0;

            for (Student classInHashmap : studentToUpdateClassList) {

                try {
                    if (classInHashmap.classSisID.equals(studentToParse.classSisID)) {
                        System.out.println("Adding another contact for  student" + classInHashmap.studentName + " for class ID " + classInHashmap.classSisID);
                        classInHashmap.addAnotherContact(studentToParse);
                        studentToUpdateClassList.set(x, classInHashmap);
                        studentsInSystem.replace(Integer.parseInt(studentToParse.studentSisID), studentToUpdateClassList);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    System.out.println("An Exception was caught trying to parse the classID for the student. ");
                }

            }
            System.out.println("Adding another entry for student " + studentToParse.studentName + " for class ID " + studentToParse.classSisID);
            studentToUpdateClassList.add(studentToParse);
            studentsInSystem.replace(Integer.parseInt(studentToParse.studentSisID), studentToUpdateClassList);

        }
    }

    public static class Student {
//        student_name,
//        student_sis_id,
//        student_grade,
//        student_language,
//        parent1_name,
//        parent1_email,
//        parent1_phone,
//        parent1_language,
//        parent2_name,
//        parent2_email,
//        parent2_phone,
//        parent2_language,
//        class_name,
//        class_sis_id

        String studentName;
        String studentSisID;
        String studentGrade;
        String studentLanguage;
        ArrayList<String> parentName;
        ArrayList<String> parentEmail;
        ArrayList<String> parentPhoneNumber;
        ArrayList<String> parentLanguage;
        String className;
        String classSisID;

        boolean contactDropped;

        Student() {
            throw new IllegalStateException(" Don't use this");
        }

        Student(String[] splitLines) {
            parentName = new ArrayList<>();
            parentEmail = new ArrayList<>();
            parentPhoneNumber = new ArrayList<>();
            parentLanguage = new ArrayList<>();


            studentName = splitLines[0];
            studentSisID = splitLines[1];
            studentGrade = splitLines[2];
            studentLanguage = splitLines[3];

            parentName.add(splitLines[4]);
            parentEmail.add(splitLines[5]);
            parentPhoneNumber.add(splitLines[6]);
            parentLanguage.add(splitLines[7]);
            //gap for empty second parent 8 9 10 11

            className = splitLines[12];
            classSisID = splitLines[13];

        }

        public static void writeOutStudent(ArrayList<Student> value, BufferedWriter csvOutput) throws IOException {
            for (Student studentToWrite : value) {
                csvOutput.write(studentToWrite.writeString());
            }
        }

        public void addAnotherContact(Student secondaryContact) {
            parentName.add(secondaryContact.parentName.get(0));
            parentEmail.add(secondaryContact.parentEmail.get(0));
            parentPhoneNumber.add(secondaryContact.parentPhoneNumber.get(0));
            parentLanguage.add(secondaryContact.parentLanguage.get(0));

        }

        public String writeString() {
            String seperator = ",";
            StringBuilder studentStringToReturn = new StringBuilder();
            studentStringToReturn.append(studentName).append(seperator);
            studentStringToReturn.append(studentSisID).append(seperator);
            studentStringToReturn.append(studentGrade).append(seperator);
            studentStringToReturn.append(studentLanguage).append(seperator);
            studentStringToReturn.append(parentName.get(0)).append(seperator);
            studentStringToReturn.append(parentEmail.get(0)).append(seperator);
            studentStringToReturn.append(parentPhoneNumber.get(0)).append(seperator);
            studentStringToReturn.append(parentLanguage.get(0)).append(seperator);
            if (parentName.size() == 1) {
                studentStringToReturn.append(",,,,");
            }
            if (parentName.size() >= 2) {
                studentStringToReturn.append(parentName.get(1)).append(seperator);
                studentStringToReturn.append(parentEmail.get(1)).append(seperator);
                studentStringToReturn.append(parentPhoneNumber.get(1)).append(seperator);
                studentStringToReturn.append(parentLanguage.get(1)).append(seperator);
            }
            if (parentName.size() > 2) {
                System.out.println("**************************************************************************************************************** ");
                System.out.println();
                System.out.println("WARNING: ONE OR MORE CONTACTS ARE BEING DROPPED BY THE PROGRAM, TOO MANY CONTACTS FOR FOLLOWING STUDENT entry: ");
                System.out.println();
                System.out.println("Student NameName: " + studentName);
                System.out.println("Student ID: " + studentSisID);
                System.out.println("Student Class ID: " + classSisID);
                System.out.println("Student Class Name: " + className);
                System.out.println();
                System.out.println("####### parent info for this student / class entry #######");

                for (int x = 0; x < parentName.size(); x++) {
                    System.out.println(kept(x) + " Parent name " + x + ": " + parentName.get(x));
                    System.out.println(kept(x) + " Parent email " + x + ": " + parentEmail.get(x));
                    System.out.println(kept(x) + " Parent Phone number" + x + ": " + parentPhoneNumber.get(x));
                    System.out.println(kept(x) + " Parent language" + x + ": " + parentLanguage.get(x));
                }
                System.out.println("##########################################################");
                System.out.println();
                System.out.println("**************************************************************************************************************** ");


            }
            studentStringToReturn.append(className).append(seperator);
            studentStringToReturn.append(classSisID);
            studentStringToReturn.append("\n");
            return studentStringToReturn.toString();
        }

        public String kept(int x) {
            if (x >= 2) {
                return "Dropped:";
            } else {
                return "Kept: ";
            }
        }
    }
}
