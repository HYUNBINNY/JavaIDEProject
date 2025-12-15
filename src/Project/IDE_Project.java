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

        // 1. UI 초기화 (메뉴바, 탭팬, 결과창)
        initUI();

        setVisible(true);
    }

    private void initUI() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());



        // --- 메뉴바 생성 ---
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(248, 222, 130));
        menuBar.setOpaque(true);
        //메뉴바 높이 지정
        menuBar.setPreferredSize(new Dimension(0, 30));

        // File 메뉴
        JMenu fileMenu = new JMenu("File");
        JMenuItem itemOpen = new JMenuItem("Open");
        JMenuItem itemClose = new JMenuItem("Close");
        JMenuItem itemSave = new JMenuItem("Save");
        JMenuItem itemSaveAs = new JMenuItem("Save As");
        JMenuItem itemQuit = new JMenuItem("Quit");

        //메뉴아이템 배경색상
        itemOpen.setBackground(new Color(100, 179, 248));
        itemClose.setBackground(new Color(100, 179, 248));
        itemSave.setBackground(new Color(100, 179, 248));
        itemSaveAs.setBackground(new Color(100, 179, 248));
        itemQuit.setBackground(new Color(100, 179, 248));

        //메뉴아이템 글씨 색깔
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

        // 단축키 키입력 조합으로 Ctrl+R지정
        itemCompile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        runMenu.add(itemCompile);

        //메뉴아이템 배경과 글씨 색지정
        itemCompile.setBackground(new Color(100, 179, 248));
        itemCompile.setForeground(Color.WHITE);

        menuBar.add(fileMenu);
        menuBar.add(runMenu);
        setJMenuBar(menuBar);

        // --- 메인 영역 ---
        tabbedPane = new JTabbedPane();
        c.add(tabbedPane, BorderLayout.CENTER);

        // --- 하단 결과창 ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        //결과창 이름과 패널 추가
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Result Window"));
        resultArea = new JTextArea(8, 100);
        resultArea.setEditable(false);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        // --- JFileChooser 설정 ---
        fileChooser = new JFileChooser();

        // 기본 디렉토리 설정
        fileChooser.setCurrentDirectory(new File("C:\\Term_Project_1\\src\\Project_1"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java Files", "java"));

        // --- 이벤트 리스너 연결 ---

        itemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        itemClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeCurrentTab();
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile(false); // Save
            }
        });

        itemSaveAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile(true); // Save As
            }
        });

        itemQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        itemCompile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compileCurrentFile();
            }
        });

    }

    // --- 기능 구현 메소드 ---

    // 1. 파일 열기 (JFileChooser + Tab 추가)
    private void openFile() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            //해당 선택 파일 경로 저장
            Path path = selectedFile.toPath();

            //결과창 출력 메서드
            if (!selectedFile.getName().endsWith(".java")) {
                appendResult("오류: .java 파일만 열 수 있습니다.");
                return;
            }

            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                //자바파일시 해당 탭 추가 메소드
                addTab(selectedFile.getName(), content, path);
                appendResult("파일 열기 성공: " + selectedFile.getName());
            } catch (IOException ex) {
                appendResult("파일 읽기 실패: " + ex.getMessage());
            }
        }
    }

    // 탭 추가 메소드
    private void addTab(String title, String content, Path path) {
        JTextArea textArea = new JTextArea(content);
        JScrollPane scrollPane = new JScrollPane(textArea);

        tabbedPane.addTab(title, scrollPane);

        //방금연 파일 포커스
        tabbedPane.setSelectedComponent(scrollPane);

        // 탭(스크롤펜)과 파일 경로 매핑 저장
        tabFileMap.put(scrollPane, path);
    }

    // 2. 현재 탭 닫기
    private void closeCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        // 탭이 선택되어 있을 때만 닫기
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
        //열린탭이 없을떄
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "저장할 열린 파일이 없습니다.");
            return;
        }

        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        //스크롤했을때 텍스트영역 가져오기
        JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
        String content = textArea.getText();
        Path currentPath = tabFileMap.get(scrollPane);


        // "Save"인데 경로가 없거나, "Save As"인 경우 -> 파일 선택기 실행
        if (currentPath == null || isSaveAs) {
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // .java 강제저장
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

            // 탭팬 제목 수정
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
        //파일 선택안되어있을떄
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

    // 기존 코드의 SwingWorker 로직 재사용 해당경로 final 지정
    private void runCompileWorker(final Path javaPath) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            private int exitCode = -1;

            protected String doInBackground() {
                appendResult("[Compile 시작] " + javaPath.getFileName());
                //javac 실행하기위해 프로세스빌더
                ProcessBuilder pb = new ProcessBuilder("javac", javaPath.getFileName().toString());
                pb.directory(javaPath.getParent().toFile());
                Charset consoleCs = Charset.forName("MS949"); // 한글 윈도우 콘솔 인코딩

                try {
                    //쓰레드시작
                    Process p = pb.start();
                    //문자열 버퍼
                    StringBuilder errBuf = new StringBuilder();
                    // 에러 스트림 읽기
                    try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), consoleCs))) {
                        String ln;
                        while ((ln = err.readLine()) != null) {
                            //한줄씩 읽어서 추가
                            errBuf.append(ln).append(System.lineSeparator());
                        }
                    }
                    //끝날떄까지 대기
                    exitCode = p.waitFor();
                    return errBuf.toString();
                } catch (Exception ex) {
                    return "[Compile 예외] " + ex.getMessage();
                }
            }

            protected void done() {
                try {
                    //doInbackground()값 가져오기
                    String resultText = get();
                    //에러가 없으면 0, 있으면 다른수 반환시 컴파일 실패
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
        //SwingWorker 작업을 실제로 시작, 순차적으로 실행 doin시키고 done 시킴
        worker.execute();
    }

    //메세지 출력
    private void appendResult(String msg) {
        resultArea.append(msg + System.lineSeparator());
        resultArea.setCaretPosition(resultArea.getDocument().getLength()); // 스크롤 자동 이동
    }

    public static void main(String[] args) {
        // Swing 스레드 안전성을 위해 invokeLater 사용 권장
        new IDE_Project();
    }
}