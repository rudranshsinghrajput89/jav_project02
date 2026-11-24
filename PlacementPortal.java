import java.io.*;
import java.util.*;

// 1. Student Model
class Student {
    String id;
    String name;
    double cgpa;

    public Student(String id, String name, double cgpa) {
        this.id = id;
        this.name = name;
        this.cgpa = cgpa;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | CGPA " + cgpa;
    }
}

// 2. Job Model (Includes PriorityQueue & Duplicate Prevention)
class Job {
    String jobId;
    String companyName;
    String role;
    double minCgpa;
    Set<String> appliedStudentIds; // Prevents duplicate applications
    PriorityQueue<Student> applicantsPQ; // Ranks higher CGPA first

    public Job(String jobId, String companyName, String role, double minCgpa) {
        this.jobId = jobId;
        this.companyName = companyName;
        this.role = role;
        this.minCgpa = minCgpa;
        this.appliedStudentIds = new HashSet<>();
        
        // Max-Heap based on CGPA (Higher CGPA gets higher priority)
        this.applicantsPQ = new PriorityQueue<>((s1, s2) -> {
            int cmp = Double.compare(s2.cgpa, s1.cgpa);
            return (cmp != 0) ? cmp : s1.id.compareTo(s2.id); // Tie-breaker by ID
        });
    }

    public boolean addApplicant(Student s) {
        if (s.cgpa < this.minCgpa) {
            System.out.println("Application rejected: " + s.name + "'s CGPA (" + s.cgpa + 
                               ") is below minimum required (" + this.minCgpa + ").");
            return false;
        }
        if (appliedStudentIds.contains(s.id)) {
            System.out.println("Duplicate application prevented: " + s.name + " has already applied to " + jobId + ".");
            return false;
        }
        appliedStudentIds.add(s.id);
        applicantsPQ.offer(s);
        return true;
    }

    // Extract sorted list using PriorityQueue + Comparator sorting verification
    public List<Student> getShortlist() {
        List<Student> shortlist = new ArrayList<>();
        PriorityQueue<Student> tempPQ = new PriorityQueue<>(applicantsPQ);
        while (!tempPQ.isEmpty()) {
            shortlist.add(tempPQ.poll());
        }
        // Explicit Comparator-based sorting safeguard
        shortlist.sort((s1, s2) -> Double.compare(s2.cgpa, s1.cgpa));
        return shortlist;
    }

    @Override
    public String toString() {
        return jobId + " | " + companyName + " (" + role + ") | Min CGPA: " + minCgpa;
    }
}

// 3. Binary Search Tree (BST) for Ordered Student Lookup
class StudentBST {
    private class Node {
        Student student;
        Node left, right;

        Node(Student student) {
            this.student = student;
        }
    }

    private Node root;

    // Insert student ordered by Student ID
    public void insert(Student student) {
        root = insertRec(root, student);
    }

    private Node insertRec(Node root, Student student) {
        if (root == null) {
            return new Node(student);
        }
        int cmp = student.id.compareToIgnoreCase(root.student.id);
        if (cmp < 0) {
            root.left = insertRec(root.left, student);
        } else if (cmp > 0) {
            root.right = insertRec(root.right, student);
        } else {
            root.student = student; // Update if ID exists
        }
        return root;
    }

    // Search student by Student ID
    public Student search(String id) {
        return searchRec(root, id);
    }

    private Student searchRec(Node root, String id) {
        if (root == null) return null;
        int cmp = id.compareToIgnoreCase(root.student.id);
        if (cmp == 0) return root.student;
        return (cmp < 0) ? searchRec(root.left, id) : searchRec(root.right, id);
    }

    // In-order traversal for displaying students in sorted order of ID
    public List<Student> getAllStudentsOrdered() {
        List<Student> list = new ArrayList<>();
        inOrderRec(root, list);
        return list;
    }

    private void inOrderRec(Node root, List<Student> list) {
        if (root != null) {
            inOrderRec(root.left, list);
            list.add(root.student);
            inOrderRec(root.right, list);
        }
    }
}

// 4. Main Placement Portal System
public class PlacementPortal {
    private static final String FILE_NAME = "placements.txt";
    private StudentBST studentTree = new StudentBST();
    private HashMap<String, Job> jobMap = new HashMap<>();
    private int studentCounter = 101; // Auto-generates ST101, ST102...
    private int jobCounter = 101;     // Auto-generates J101, J102...

    // Add Student (Auto-ID or Custom ID)
    public void addStudent(String name, double cgpa) {
        String id = "ST" + studentCounter++;
        while (studentTree.search(id) != null) {
            id = "ST" + studentCounter++;
        }
        Student s = new Student(id, name, cgpa);
        studentTree.insert(s);
        System.out.println("Student added: " + s);
    }

    public void addStudentWithId(String id, String name, double cgpa) {
        Student s = new Student(id.toUpperCase(), name, cgpa);
        studentTree.insert(s);
        System.out.println("Student added: " + s);
    }

    // Add Job
    public String addJob(String company, String role, double minCgpa) {
        String jobId = "J" + jobCounter++;
        while (jobMap.containsKey(jobId)) {
            jobId = "J" + jobCounter++;
        }
        Job job = new Job(jobId, company, role, minCgpa);
        jobMap.put(jobId, job);
        System.out.println("Job added: " + job);
        return jobId;
    }

    public void addJobWithId(String jobId, String company, String role, double minCgpa) {
        jobId = jobId.toUpperCase();
        Job job = new Job(jobId, company, role, minCgpa);
        jobMap.put(jobId, job);
        System.out.println("Job added: " + job);
    }

    // Search Job using HashMap (O(1) lookup)
    public Job searchJob(String jobId) {
        Job job = jobMap.get(jobId.toUpperCase());
        if (job == null) {
            System.out.println("Job " + jobId + " not found.");
        } else {
            System.out.println("Job Found: " + job);
        }
        return job;
    }

    // Search Student using BST (O(log n) lookup)
    public Student searchStudent(String studentId) {
        Student s = studentTree.search(studentId.toUpperCase());
        if (s == null) {
            System.out.println("Student " + studentId + " not found.");
        } else {
            System.out.println("Student Found: " + s);
        }
        return s;
    }

    // Apply for a Job
    public void applyForJob(String studentId, String jobId) {
        Student s = studentTree.search(studentId.toUpperCase());
        if (s == null) {
            System.out.println("Student " + studentId + " not found.");
            return;
        }
        Job j = jobMap.get(jobId.toUpperCase());
        if (j == null) {
            System.out.println("Job " + jobId + " not found.");
            return;
        }
        if (j.addApplicant(s)) {
            System.out.println(s.name + " (" + s.id + ") successfully applied to " + j.jobId + ".");
        }
    }

    // Display Shortlist for a Job
    public void displayShortlist(String jobId) {
        Job j = jobMap.get(jobId.toUpperCase());
        if (j == null) {
            System.out.println("Job " + jobId + " not found.");
            return;
        }
        List<Student> shortlist = j.getShortlist();
        if (shortlist.isEmpty()) {
            System.out.println("Shortlist for " + jobId + ": No eligible applicants yet.");
            return;
        }
        if (shortlist.size() == 1) {
            Student s = shortlist.get(0);
            System.out.println("Shortlist: " + s.id + " | " + s.name + " | " + s.cgpa);
        } else {
            System.out.println("Shortlist:");
            for (int i = 0; i < shortlist.size(); i++) {
                Student s = shortlist.get(i);
                System.out.println((i + 1) + ". " + s.name + " | " + s.cgpa);
            }
        }
    }

    // Display All Students (In-Order BST Traversal)
    public void displayAllStudents() {
        List<Student> students = studentTree.getAllStudentsOrdered();
        if (students.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }
        System.out.println("\n--- Registered Students (Ordered by ID via BST) ---");
        for (Student s : students) {
            System.out.println(s);
        }
    }

    // Display All Jobs
    public void displayAllJobs() {
        if (jobMap.isEmpty()) {
            System.out.println("No job openings available.");
            return;
        }
        System.out.println("\n--- Available Job Openings ---");
        for (Job j : jobMap.values()) {
            System.out.println(j);
        }
    }

    // File Handling: Save data to placements.txt on exit
    public void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            // Save Students
            for (Student s : studentTree.getAllStudentsOrdered()) {
                writer.println("STUDENT|" + s.id + "|" + s.name + "|" + s.cgpa);
            }
            // Save Jobs and their Applicants
            for (Job j : jobMap.values()) {
                String applicantsStr = String.join(",", j.appliedStudentIds);
                writer.println("JOB|" + j.jobId + "|" + j.companyName + "|" + j.role + "|" + j.minCgpa + "|" + applicantsStr);
            }
            System.out.println("Data successfully saved to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // File Handling: Load data from placements.txt at startup
    public void loadData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        List<String[]> jobLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals("STUDENT") && parts.length == 4) {
                    String id = parts[1];
                    String name = parts[2];
                    double cgpa = Double.parseDouble(parts[3]);
                    studentTree.insert(new Student(id, name, cgpa));
                    
                    // Keep counter updated
                    if (id.startsWith("ST")) {
                        int num = Integer.parseInt(id.substring(2));
                        if (num >= studentCounter) studentCounter = num + 1;
                    }
                } else if (parts[0].equals("JOB") && parts.length >= 5) {
                    jobLines.add(parts);
                }
            }

            // Reconstruct Jobs and re-link Applicants into PriorityQueue
            for (String[] parts : jobLines) {
                String jobId = parts[1];
                String company = parts[2];
                String role = parts[3];
                double minCgpa = Double.parseDouble(parts[4]);
                Job job = new Job(jobId, company, role, minCgpa);

                if (parts.length == 6 && !parts[5].isEmpty()) {
                    String[] appIds = parts[5].split(",");
                    for (String appId : appIds) {
                        Student s = studentTree.search(appId);
                        if (s != null) {
                            job.addApplicant(s);
                        }
                    }
                }
                jobMap.put(jobId, job);

                if (jobId.startsWith("J")) {
                    int num = Integer.parseInt(jobId.substring(1));
                    if (num >= jobCounter) jobCounter = num + 1;
                }
            }
            System.out.println("Loaded previous placement data from " + FILE_NAME);
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    // Automated Verification of the 4 Required Test Cases
    public void runTestCases() {
        System.out.println("\n========== RUNNING TEST CASES ==========");
        
        // Test Case 1: Normal Input
        System.out.println("\n[Test Case 1: Normal Input]");
        addStudentWithId("ST101", "Aman", 8.4);

        // Test Case 2: Boundary / Minimum Input
        System.out.println("\n[Test Case 2: Boundary / Minimum Input]");
        addJobWithId("J101", "TCS", "Software Engineer", 7.0);
        applyForJob("ST101", "J101");
        displayShortlist("J101");

        // Test Case 3: Invalid Input
        System.out.println("\n[Test Case 3: Invalid Input]");
        searchJob("J999");

        // Test Case 4: Different Valid Scenario
        System.out.println("\n[Test Case 4: Different Valid Scenario]");
        addJobWithId("J102", "Infosys", "System Engineer", 7.5);
        addStudentWithId("ST102", "Riya", 9.1);
        applyForJob("ST101", "J102");
        applyForJob("ST102", "J102");
        displayShortlist("J102");
        
        System.out.println("\n========================================");
    }

    // Interactive Menu
    public static void main(String[] args) {
        PlacementPortal portal = new PlacementPortal();
        portal.loadData(); // Load from placements.txt on startup

        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== RUNGTA PLACEMENT PORTAL (PS-56) ===");
            System.out.println("1. Add Student");
            System.out.println("2. Add Job Opening");
            System.out.println("3. Search Student (BST)");
            System.out.println("4. Search Job (HashMap)");
            System.out.println("5. Apply for Job");
            System.out.println("6. View Job Shortlist (Priority Queue)");
            System.out.println("7. Display All Students");
            System.out.println("8. Display All Jobs");
            System.out.println("9. Run Built-in Test Cases (Cases 1-4)");
            System.out.println("0. Save & Exit");
            System.out.print("Enter choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("Invalid input. Enter a number: ");
                sc.next();
            }
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter Student Name: ");
                    String sName = sc.nextLine();
                    System.out.print("Enter CGPA: ");
                    double cgpa = sc.nextDouble();
                    portal.addStudent(sName, cgpa);
                    break;

                case 2:
                    System.out.print("Enter Company Name: ");
                    String company = sc.nextLine();
                    System.out.print("Enter Job Role: ");
                    String role = sc.nextLine();
                    System.out.print("Enter Minimum CGPA Required: ");
                    double minCgpa = sc.nextDouble();
                    portal.addJob(company, role, minCgpa);
                    break;

                case 3:
                    System.out.print("Enter Student ID (e.g., ST101): ");
                    String searchSid = sc.nextLine();
                    portal.searchStudent(searchSid);
                    break;

                case 4:
                    System.out.print("Enter Job ID (e.g., J101): ");
                    String searchJid = sc.nextLine();
                    portal.searchJob(searchJid);
                    break;

                case 5:
                    System.out.print("Enter Student ID: ");
                    String appSid = sc.nextLine();
                    System.out.print("Enter Job ID: ");
                    String appJid = sc.nextLine();
                    portal.applyForJob(appSid, appJid);
                    break;

                case 6:
                    System.out.print("Enter Job ID to view shortlist: ");
                    String shortJid = sc.nextLine();
                    portal.displayShortlist(shortJid);
                    break;

                case 7:
                    portal.displayAllStudents();
                    break;

                case 8:
                    portal.displayAllJobs();
                    break;

                case 9:
                    portal.runTestCases();
                    break;

                case 0:
                    portal.saveData(); // Save to placements.txt on exit
                    System.out.println("Exiting Placement Portal. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);

        sc.close();
    }
}