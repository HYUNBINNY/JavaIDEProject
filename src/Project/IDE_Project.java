package Project;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class IDE_Project extends JFrame {
    // UI 컴포넌트
    private JTabbedPane tabbedPane;
    private JTextArea resultArea;
    private JFileChooser fileChooser;

    // 탭별 파일 경로 관리를 위한 맵 (Key: ScrollPane, Value: Path)
    private Map<Component, Path> tabFileMap = new HashMap<>();

    public IDE_Project() {
        super("자바 프로그램 개발용 GUI IDE [Term Project 3]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // 화면 중앙 배치

        // 1. UI 초기화 (메뉴바, 탭팬, 결과창)
        initUI();

        setVisible(true);
    }

    private void initUI() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());



        // --- 메뉴바 생성 (Requirement 1) ---
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(248, 222, 130));
        menuBar.setOpaque(true);
        menuBar.setPreferredSize(new Dimension(0, 30));

        // File 메뉴
        JMenu fileMenu = new JMenu("File");
        JMenuItem itemOpen = new JMenuItem("Open");
        JMenuItem itemClose = new JMenuItem("Close");
        JMenuItem itemSave = new JMenuItem("Save");
        JMenuItem itemSaveAs = new JMenuItem("Save As");
        JMenuItem itemQuit = new JMenuItem("Quit");

        itemOpen.setBackground(new Color(100, 179, 248));
        itemClose.setBackground(new Color(100, 179, 248));
        itemSave.setBackground(new Color(100, 179, 248));
        itemSaveAs.setBackground(new Color(100, 179, 248));
        itemQuit.setBackground(new Color(100, 179, 248));

        itemOpen.setForeground(Color.WHITE);
        itemClose.setForeground(Color.WHITE);
        itemSave.setForeground(Color.WHITE);
        itemSaveAs.setForeground(Color.WHITE);
        itemQuit.setForeground(Color.WHITE);

        fileMenu.add(itemOpen);
        fileMenu.add(itemClose);
        fileMenu.addSeparator();
        fileMenu.add(itemSave);
        fileMenu.add(itemSaveAs);
        fileMenu.addSeparator();
        fileMenu.add(itemQuit);

        // Run 메뉴
        JMenu runMenu = new JMenu("Run");
        JMenuItem itemCompile = new JMenuItem("Compile");
        // 단축키 Ctrl+R 설정 (Requirement 4)
        itemCompile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        runMenu.add(itemCompile);

        itemCompile.setBackground(new Color(100, 179, 248));

        itemCompile.setForeground(Color.WHITE);

        menuBar.add(fileMenu);
        menuBar.add(runMenu);
        setJMenuBar(menuBar);

        // --- 메인 영역 (JTabbedPane) (Requirement 2) ---
        tabbedPane = new JTabbedPane();
        c.add(tabbedPane, BorderLayout.CENTER);

        // --- 하단 결과창 (Result Window) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Result Window"));
        resultArea = new JTextArea(8, 100);
        resultArea.setEditable(false);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        // --- JFileChooser 설정 ---
        fileChooser = new JFileChooser();
        // 기본 디렉토리 설정 (기존 코드 유지)
        fileChooser.setCurrentDirectory(new File("C:\\Term_Project_1\\src\\Project_1"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java Files", "java"));

        // --- 이벤트 리스너 연결 ---
        itemOpen.addActionListener(e -> openFile());
        itemClose.addActionListener(e -> closeCurrentTab());
        itemSave.addActionListener(e -> saveFile(false)); // false = Save
        itemSaveAs.addActionListener(e -> saveFile(true)); // true = Save As
        itemQuit.addActionListener(e -> System.exit(0));
        itemCompile.addActionListener(e -> compileCurrentFile());
    }

    // --- 기능 구현 메소드 ---

    // 1. 파일 열기 (JFileChooser + Tab 추가)
    private void openFile() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Path path = selectedFile.toPath();

            if (!selectedFile.getName().endsWith(".java")) {
                appendResult("오류: .java 파일만 열 수 있습니다.");
                return;
            }

            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                addTab(selectedFile.getName(), content, path);
                appendResult("파일 열기 성공: " + selectedFile.getName());
            } catch (IOException ex) {
                appendResult("파일 읽기 실패: " + ex.getMessage());
            }
        }
    }

    // 탭 추가 헬퍼 메소드
    private void addTab(String title, String content, Path path) {
        JTextArea textArea = new JTextArea(content);
        JScrollPane scrollPane = new JScrollPane(textArea);

        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        // 탭(스크롤펜)과 파일 경로 매핑 저장
        tabFileMap.put(scrollPane, path);
    }

    // 2. 현재 탭 닫기
    private void closeCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            Component c = tabbedPane.getComponentAt(index);
            tabFileMap.remove(c); // 맵에서 제거
            tabbedPane.remove(index); // 탭 제거
            appendResult("탭이 닫혔습니다.");
        }
    }

    // 3. 파일 저장 (Save & Save As)
    private void saveFile(boolean isSaveAs) {
        int index = tabbedPane.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "저장할 열린 파일이 없습니다.");
            return;
        }

        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
        String content = textArea.getText();
        Path currentPath = tabFileMap.get(scrollPane);

        // "Save"인데 경로가 없거나, "Save As"인 경우 -> 파일 선택기 실행
        if (currentPath == null || isSaveAs) {
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // 확장자 처리
                if (!fileToSave.getName().endsWith(".java")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".java");
                }

                // 이미 존재하는 파일인지 확인 (Requirement 6)
                if (fileToSave.exists()) {
                    int response = JOptionPane.showConfirmDialog(this,
                            fileToSave.getName() + " 이(가) 이미 존재합니다.\n덮어쓰시겠습니까?",
                            "파일 덮어쓰기 경고",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (response != JOptionPane.YES_OPTION) {
                        return; // 저장 취소
                    }
                }

                currentPath = fileToSave.toPath();
            } else {
                return; // 다이얼로그 취소
            }
        } else {
            // "Save" (기존 경로 존재) -> 확인 다이얼로그 (Requirement 7)
            int response = JOptionPane.showConfirmDialog(this,
                    "파일을 저장하시겠습니까?",
                    "저장 확인",
                    JOptionPane.OK_CANCEL_OPTION);
            if (response != JOptionPane.OK_OPTION) {
                return;
            }
        }

        // 실제 파일 쓰기
        try {
            Files.writeString(currentPath, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 탭 제목 및 맵 정보 업데이트
            tabbedPane.setTitleAt(index, currentPath.getFileName().toString());
            tabFileMap.put(scrollPane, currentPath);
            appendResult("파일 저장 성공: " + currentPath.getFileName());

        } catch (IOException ex) {
            appendResult("파일 저장 실패: " + ex.getMessage());
        }
    }

    // 4. 컴파일 (기존 로직 활용)
    private void compileCurrentFile() {
        int index = tabbedPane.getSelectedIndex();
        if (index == -1) {
            appendResult("오류: 컴파일할 파일이 선택되지 않았습니다.");
            return;
        }

        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        Path path = tabFileMap.get(scrollPane);

        if (path == null || !Files.exists(path)) {
            appendResult("오류: 파일이 저장되지 않았습니다. 컴파일 전에 먼저 저장하세요.");
            return;
        }

        // 기존 Worker 로직 실행
        runCompileWorker(path);
    }

    // 기존 코드의 SwingWorker 로직 재사용
    private void runCompileWorker(final Path javaPath) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            private int exitCode = -1;

            protected String doInBackground() {
                appendResult("[Compile 시작] " + javaPath.getFileName());
                ProcessBuilder pb = new ProcessBuilder("javac", javaPath.getFileName().toString());
                pb.directory(javaPath.getParent().toFile());
                Charset consoleCs = Charset.forName("MS949"); // 한글 윈도우 콘솔 인코딩

                try {
                    Process p = pb.start();
                    StringBuilder errBuf = new StringBuilder();
                    // 에러 스트림 읽기
                    try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), consoleCs))) {
                        String ln;
                        while ((ln = err.readLine()) != null) {
                            errBuf.append(ln).append(System.lineSeparator());
                        }
                    }
                    exitCode = p.waitFor();
                    return errBuf.toString();
                } catch (Exception ex) {
                    return "[Compile 예외] " + ex.getMessage();
                }
            }

            protected void done() {
                try {
                    String resultText = get();
                    if (exitCode == 0) {
                        appendResult("컴파일 성공 (Exit code: 0)");
                        appendResult("Class file created at: " + javaPath.getParent());
                    } else {
                        appendResult("[Compile 실패] (Exit code: " + exitCode + ")");
                        appendResult(resultText.isBlank() ? "(오류 메시지 없음)" : resultText);
                    }
                } catch (Exception e) {
                    appendResult("[Worker 오류] " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void appendResult(String msg) {
        resultArea.append(msg + System.lineSeparator());
        resultArea.setCaretPosition(resultArea.getDocument().getLength()); // 스크롤 자동 이동
    }

    public static void main(String[] args) {
        // Swing 스레드 안전성을 위해 invokeLater 사용 권장
        new IDE_Project();
    }
}