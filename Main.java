import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

abstract class FileObject {
    protected String fileName;
    protected String extension;
    protected long createdTime;
    protected long lastModifiedTime;

    public FileObject(String fileName, String extension, long createdTime, long lastModifiedTime) {
        this.fileName = fileName;
        this.extension = extension;
        this.createdTime = createdTime;
        this.lastModifiedTime = lastModifiedTime;
    }

    public abstract String getInfo();
}

class TextFile extends FileObject {
    private int lineCount;
    private int wordCount;
    private int charCount;

    public TextFile(String fileName, String extension, long createdTime, long lastModifiedTime,
                    int lineCount, int wordCount, int charCount) {
        super(fileName, extension, createdTime, lastModifiedTime);
        this.lineCount = lineCount;
        this.wordCount = wordCount;
        this.charCount = charCount;
    }

    public String getInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModified = sdf.format(new Date(lastModifiedTime));
        String created = sdf.format(new Date(createdTime));

        return String.format("File name: %s\nFile extension: %s\nLast modified: %s\nDate created: %s\nLine count: %d\nWord count: %d\nCharacter count: %d",
                fileName, extension, lastModified, created, lineCount, wordCount, charCount);
    }
}

class ImageFile extends FileObject {
    private int width;
    private int height;

    public ImageFile(String fileName, String extension, long createdTime, long lastModifiedTime,
                     int width, int height) {
        super(fileName, extension, createdTime, lastModifiedTime);
        this.width = width;
        this.height = height;
    }

    public String getInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModified = sdf.format(new Date(lastModifiedTime));
        String created = sdf.format(new Date(createdTime));

        return String.format("File name: %s\nFile extension: %s\nLast modified: %s\nDate created: %s\nImage size: %d x %d",
                fileName, extension, lastModified, created, width, height);
    }
}

class ProgramFile extends FileObject {
    private int lineCount;
    private int classCount;
    private int methodCount;

    public ProgramFile(String fileName, String extension, long createdTime, long lastModifiedTime,
                       int lineCount, int classCount, int methodCount) {
        super(fileName, extension, createdTime, lastModifiedTime);
        this.lineCount = lineCount;
        this.classCount = classCount;
        this.methodCount = methodCount;
    }

    public String getInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModified = sdf.format(new Date(lastModifiedTime));
        String created = sdf.format(new Date(createdTime));

        return String.format("File name: %s\nFile extension: %s\nLast modified: %s\nDate created: %s\nLine count: %d\nClass count: %d\nMethod count: %d",
                fileName, extension, lastModified, created, lineCount, classCount, methodCount);
    }
}


class ImageFile extends FileObject {
    private int width;
    private int height;

    public ImageFile(String fileName, String extension, long createdTime, long lastModifiedTime,
                     int width, int height) {
        super(fileName, extension, createdTime, lastModifiedTime);
        this.width = width;
        this.height = height;
    }

   
    public String getInfo() {
        return String.format("%s.%s - Width: %d, Height: %d",
                fileName, extension, width, height);
    }
}

class ProgramFile extends FileObject {
    private int lineCount;
    private int classCount;
    private int methodCount;

    public ProgramFile(String fileName, String extension, long createdTime, long lastModifiedTime,
                       int lineCount, int classCount, int methodCount) {
        super(fileName, extension, createdTime, lastModifiedTime);
        this.lineCount = lineCount;
        this.classCount = classCount;
        this.methodCount = methodCount;
    }

    
    public String getInfo() {
        return String.format("%s.%s - Line count: %d, Class count: %d, Method count: %d",
                fileName, extension, lineCount, classCount, methodCount);
    }
}

class FileChangeDetector {
    private String directoryPath;
    Map<String, FileObject> fileMap;
    private FileWriter writer;
    private Set<String> changedFiles = new HashSet<>();

    public FileChangeDetector(String directoryPath) {
        this.directoryPath = directoryPath;
        this.fileMap = new HashMap<>();
        System.currentTimeMillis();

        File file = new File("operatii.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            this.writer = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        detectChanges(); 

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
           
            public void run() {
                detectChanges();
                commit();
            }
        }, 0, 5000);
    }

    public void detectChanges() {
        System.out.println("Detecting changes...");
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                long lastModifiedTime = file.lastModified();

                if (fileMap.containsKey(fileName)) {
                    FileObject fileObject = fileMap.get(fileName);
                    if (fileObject.lastModifiedTime != lastModifiedTime) {
                        changedFiles.add(fileName);
                        fileObject.lastModifiedTime = lastModifiedTime;
                    }
                } else {
                    try {
                        writer.write(fileName + " - New file\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addFileObject(file); 
                    
                }
            }
        }

        Set<String> removedFiles = new HashSet<>(fileMap.keySet());
        removedFiles.removeAll(Arrays.asList(directory.list()));

        for (String removedFile : removedFiles) {
            fileMap.remove(removedFile);
            try {
                writer.write(removedFile + " - Deleted\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        changedFiles.retainAll(fileMap.keySet());
    }


    public void commit() {
        long currentSnapshotTime = System.currentTimeMillis();
        String snapshotTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentSnapshotTime));
        try {
            writer.write("Snapshot created at: " + snapshotTimeString + "\n");

            for (String fileName : fileMap.keySet()) {
                if (!changedFiles.contains(fileName)) {
                    writer.write(fileName + " - Not modified\n");
                }
            }

            for (String fileName : changedFiles) {
                writer.write(fileName + " - Changed\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        changedFiles.clear();
        detectChanges();
        writeSnapshot();
    
    }
    


    private void addFileObject(File file) {
        String fileName = file.getName();
        String extension = getFileExtension(fileName);
        long lastModifiedTime = file.lastModified();

        if (extension.equals("txt")) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                int lineCount = 0;
                int wordCount = 0;
                int charCount = 0;
                String line;
                while ((line = br.readLine()) != null) {
                    lineCount++;
                    charCount += line.length();
                    String[] words = line.split("\\s+");
                    wordCount += words.length;
                }
                fileMap.put(fileName, new TextFile(fileName, extension, lastModifiedTime, lastModifiedTime, lineCount, wordCount, charCount));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (extension.equals("jpg") || extension.equals("png")) {
            try {
                BufferedImage image = ImageIO.read(file);
                int width = image.getWidth();
                int height = image.getHeight();
                fileMap.put(fileName, new ImageFile(fileName, extension, lastModifiedTime, lastModifiedTime, width, height));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (extension.equals("java") || extension.equals("py")) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                int lineCount = 0;
                int classCount = 0;
                int methodCount = 0;
                String line;
                while ((line = br.readLine()) != null) {
                    lineCount++;
                    if (line.trim().startsWith("class") || line.trim().startsWith("public class")) {
                        classCount++;
                    }
                    if (line.trim().startsWith("def")) {
                        methodCount++;
                    }
                }
                fileMap.put(fileName, new ProgramFile(fileName, extension, lastModifiedTime, lastModifiedTime, lineCount, classCount, methodCount));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fileMap.put(fileName, new FileObject(fileName, extension, lastModifiedTime, lastModifiedTime) {
                @Override
                public String getInfo() {
                    return fileName + "." + extension;
                }
            });
        }
    }

    public void getInfo(String fileName) {
        FileObject fileObject = fileMap.get(fileName);
        if (fileObject != null) {
            System.out.println(fileObject.getInfo());
        } else {
            System.out.println("File not found.");
        }
    }

    public void status() {
        // Creează un snapshot înainte de a afișa starea
        writeSnapshot();
    
        System.out.println("Status:");
        long currentSnapshotTime = System.currentTimeMillis();
        String snapshotTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentSnapshotTime));
        System.out.println("Snapshot created at: " + snapshotTimeString);
    
        File directory = new File(directoryPath);
        String[] fileList = directory.list();
    
        for (String fileName : fileList) {
            File file = new File(directoryPath + File.separator + fileName);
            long lastModifiedTime = file.lastModified();
            if (fileMap.containsKey(fileName)) {
                FileObject fileObject = fileMap.get(fileName);
                if (fileObject.lastModifiedTime == lastModifiedTime) {
                    System.out.println(fileName + " - Not modified");
                } else {
                    System.out.println(fileName + " - Changed");
                }
            } else {
                System.out.println(fileName + " - New file");
            }
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeSnapshot() {
        long currentSnapshotTime = System.currentTimeMillis();
        String snapshotTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentSnapshotTime));
        try {
            // Scrie snapshot-ul în fișier
            writer.write("Snapshot created at: " + snapshotTimeString + "\n");
            for (String fileName : fileMap.keySet()) {
                if (!changedFiles.contains(fileName)) {
                    writer.write(fileName + " - Not modified\n");
                }
            }
            for (String fileName : changedFiles) {
                writer.write(fileName + " - Changed\n");
            }
            writer.flush();
            
            // Afișează snapshot-ul în consolă
            System.out.println("Snapshot created at: " + snapshotTimeString);
            for (String fileName : fileMap.keySet()) {
                if (!changedFiles.contains(fileName)) {
                    System.out.println(fileName + " - Not modified");
                }
            }
            for (String fileName : changedFiles) {
                System.out.println(fileName + " - Changed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        changedFiles.clear();
    }
    
    
 
    }


public class Main {
    public static void main(String[] args) {
        String directoryPath = "C:\\Users\\User\\Desktop\\Laborator OOP Nr3";

        FileChangeDetector detector = new FileChangeDetector(directoryPath);
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.println("Select an option:");
                System.out.println("1. Commit");
                System.out.println("2. Info");
                System.out.println("3. Status");
                System.out.println("0. Exit");

                System.out.print("> ");
                String input = scanner.nextLine();

                switch (input) {
                    case "1":
                        detector.commit();
                        break;
                        case "2":
                        System.out.print("Enter the filename: ");
                        String fileName = scanner.nextLine();
                        FileObject fileObject = detector.fileMap.get(fileName);
                        if (fileObject != null && fileObject instanceof TextFile) {
                            System.out.println(((TextFile) fileObject).getInfo());
                        } else {
                            detector.getInfo(fileName);
                        }
                        break;
                    case "3":
                        detector.status();
                        break;
                    case "0":
                        scanner.close();
                        detector.closeWriter();
                        System.exit(0);
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            }
        } finally {
            scanner.close();
            detector.closeWriter();
        }
    }
}
