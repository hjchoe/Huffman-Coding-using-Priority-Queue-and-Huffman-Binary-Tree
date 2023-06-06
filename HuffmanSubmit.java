import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HuffmanSubmit {
    class HuffmanTreeVisualizer extends JPanel implements Runnable {
        private Node root;
        private PriorityQueue<Node> pq;
        private int delay;

        HuffmanTreeVisualizer(Node root, PriorityQueue<Node> pq, int delay) {
            this.pq = new PriorityQueue<>(pq);
            this.delay = delay;
        }

        public Node animate() {
            final Node[] animatedRoot = new Node[1];
            Timer timer = new Timer(delay, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (pq.size() > 1) {
                        Node left = pq.poll();
                        Node right = pq.poll();
                        int sum = left.freq + right.freq;
                        pq.add(new Node(null, sum, left, right));

                        animatedRoot[0] = pq.peek();
                        root = animatedRoot[0];
                        repaint();
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            timer.start();

            // Wait for the animation to finish before returning the root node
            while (timer.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return animatedRoot[0];
        }

        @Override
        public void run() {
            while (pq.size() > 1) {
                try {
                    Node left = pq.poll();
                    Node right = pq.poll();
                    int sum = left.freq + right.freq;
                    Node newNode = new Node(null, sum, left, right);
                    pq.add(newNode);

                    repaint(); // Refresh the display
                    Thread.sleep(delay); // Add delay for the animation

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.root = pq.peek();

            synchronized (this) {
                notify();
            }
        }

        private int getTreeHeight(Node root) {
            if (root == null) {
                return 0;
            }
            return Math.max(getTreeHeight(root.left), getTreeHeight(root.right)) + 1;
        }

        private void drawNode(Graphics g, Node node, int x, int y) {
            int nodeWidth = 50;
            int nodeHeight = 30;

            g.setColor(Color.BLACK);
            g.drawRect(x, y, nodeWidth, nodeHeight);

            String nodeLabel = node.ch == null ? String.valueOf(node.freq) : (node.ch + ":" + node.freq);
            g.drawString(nodeLabel, x + nodeWidth / 4, y + nodeHeight / 2 + 5);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();

            if (root != null) {
                int treeHeight = getTreeHeight(root);
                drawTree(g, root, getWidth() / 2 - 25, 20, 0, treeHeight);
            }

            int x = 10;
            int y = height - 50;
            for (Node node : pq) {
                drawNode(g, node, x, y);
                x += 60;
            }
        }

        private void drawTree(Graphics g, Node node, int x, int y, int level, int maxLevel) {
            if (node == null) {
                return;
            }

            int xSpacing = getWidth() / (int) Math.pow(2, level + 1);
            int ySpacing = getHeight() / (maxLevel + 1);

            drawNode(g, node, x, y);

            if (node.left != null) {
                g.drawLine(x + 25, y + 30, x - xSpacing + 25, y + ySpacing + 30);
                drawTree(g, node.left, x - xSpacing, y + ySpacing, level + 1, maxLevel);
            }

            if (node.right != null) {
                g.drawLine(x + 25, y + 30, x + xSpacing + 25, y + ySpacing + 30);
                drawTree(g, node.right, x + xSpacing, y + ySpacing, level + 1, maxLevel);
            }
        }

    }

    class Node {
        Character ch;
        Integer freq;
        Node left = null;
        Node right = null;

        Node(Character ch, Integer freq) {
            this.ch = ch;
            this.freq = freq;
        }

        public Node(Character ch, Integer freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }
    }

    class Frequency {
        HashMap<Character, Integer> freq;

        Frequency() {
            this.freq = new HashMap<>();
        }

        HashMap<Character, Integer> getFreq(String input) {
            freq.clear();
            char[] data = input.toCharArray();

            for (int i = 0; i < data.length; i++) {
                char c = data[i];
                freq.put(c, freq.getOrDefault(c, 0) + 1);
            }

            freq.forEach((key, val) -> System.out.println(key + ":" + Integer.toString(val)));

            return freq;
        }

        void writeFreq(String outputPath) {
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(outputPath));
                freq.forEach((key, val) -> {
                    try {
                        String binstring = String.format("%8s", Integer.toBinaryString(key & 0xFF)).replace(' ', '0');
                        bw.write(binstring + ":" + Integer.toString(val));
                        bw.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void writeFreq(HashMap<Character, Integer> fr, String outputPath) {
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(outputPath));
                fr.forEach((key, val) -> {
                    try {
                        String binstring = String.format("%8s", Integer.toBinaryString(key & 0xFF)).replace(' ', '0');
                        bw.write(binstring + ":" + Integer.toString(val));
                        bw.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void readFreq(String freqPath) {
            freq.clear();
            try {
                BufferedReader br = new BufferedReader(new FileReader(freqPath));
                String[] data;
                if (br.ready()) {
                    String str = "";
                    while ((str = br.readLine()) != null) {
                        data = str.split(":", 2);
                        int parseInt = Integer.parseInt(data[0], 2);
                        freq.put(Character.valueOf((char) parseInt), Integer.parseInt(data[1]));
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class HuffmanCode {
        HashMap<Character, String> huffmanCode;
        HashMap<Character, Integer> freq;
        PriorityQueue<Node> pq;
        Node root;

        class MyComparator implements Comparator<Node> {
            public int compare(Node x, Node y) {
                return x.freq - y.freq;
            }
        }

        HuffmanCode(HashMap<Character, Integer> freq) {
            this.huffmanCode = new HashMap<>();
            this.freq = freq;
            createHuffTree(freq);
        }

        boolean isLeaf(Node root) {
            // returns true if both conditions return true
            return root.left == null && root.right == null;
        }

        private Node createHuffTree(Map<Character, Integer> freq) {
            pq = new PriorityQueue<>(new MyComparator());
            for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
                // creates a leaf node and add it to the queue
                pq.add(new Node(entry.getKey(), entry.getValue()));
            }

            int animationDelay = 50;
            HuffmanTreeVisualizer visualizer = new HuffmanTreeVisualizer(root, pq, animationDelay);

            // Set up the JFrame
            JFrame frame = new JFrame("Huffman Tree Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(visualizer);
            frame.setPreferredSize(new Dimension(1000, 800));
            frame.getContentPane().setBackground(new Color(182, 255, 255));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setFocusable(false);
            frame.setLayout(null);
            frame.setVisible(true);

            // Start the animation
            return this.root = visualizer.animate();

            /*
             * while (pq.size() > 1)
             * {
             * Node left = pq.poll();
             * Node right = pq.poll();
             * int sum = left.freq + right.freq;
             * pq.add(new Node(null, sum, left, right));
             * }
             * 
             * return this.root = pq.peek();
             */
        }

        void encodeData(Node root, String str) {
            if (root == null) {
                return;
            }
            if (isLeaf(root)) {
                huffmanCode.put(root.ch, str.length() > 0 ? str : "1");
            }
            encodeData(root.left, str + '0');
            encodeData(root.right, str + '1');
        }

        int decodeData(Node root, int index, String in, StringBuilder out) {
            if (root == null) {
                return index;
            }
            if (isLeaf(root)) {
                out.append(root.ch);
                return index;
            }
            index++;
            if (index >= in.length()) {
                return index;
            }
            root = (in.charAt(index) == '0') ? root.left : root.right;
            index = decodeData(root, index, in, out);
            return index;
        }
    }

    class BinaryIn {
        private BufferedInputStream in; // the input stream
        private int buffer; // one character buffer
        private int n;
        private static final int EOF = -1;

        BinaryIn(String name) {
            try {
                // first try to read file from local file system
                File file = new File(name);
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    in = new BufferedInputStream(fis);
                    fillBuffer();
                    return;
                }

                // next try for files included in jar
                URL url = getClass().getResource(name);

                // or URL from web
                if (url == null) {
                    url = new URL(name);
                }

                URLConnection site = url.openConnection();
                InputStream is = site.getInputStream();
                in = new BufferedInputStream(is);
                fillBuffer();
            } catch (IOException ioe) {
                System.err.println("Could not open " + in);
            }
        }

        public boolean isEmpty() {
            return buffer == EOF;
        }

        private void fillBuffer() {
            try {
                buffer = in.read();
                n = 8;
            } catch (IOException e) {
                System.err.println("EOF");
                buffer = EOF;
                n = -1;
            }
        }

        private boolean readBoolean() {
            if (isEmpty())
                throw new NoSuchElementException("Reading from empty input stream");
            n--;
            boolean bit = ((buffer >> n) & 1) == 1;
            if (n == 0)
                fillBuffer();
            return bit;
        }

        private char readChar() {
            if (isEmpty())
                throw new NoSuchElementException("Reading from empty input stream");

            // special case when aligned byte
            if (n == 8) {
                int x = buffer;
                fillBuffer();
                return (char) (x & 0xff);
            }

            // combine last N bits of current buffer with first 8-N bits of new buffer
            int x = buffer;
            x <<= (8 - n);
            int oldN = n;
            fillBuffer();
            if (isEmpty())
                throw new NoSuchElementException("Reading from empty input stream");
            n = oldN;
            x |= (buffer >>> n);
            return (char) (x & 0xff);
            // the above code doesn't quite work for the last character if N = 8
            // because buffer will be -1
        }

        private String readString() {
            if (isEmpty())
                throw new NoSuchElementException("Reading from empty input stream");

            StringBuilder sb = new StringBuilder();
            while (!isEmpty()) {
                char c = readChar();
                sb.append(c);
            }
            return sb.toString();
        }

        private String readBits() {
            if (isEmpty())
                throw new NoSuchElementException("Reading from empty input stream");

            StringBuilder sb = new StringBuilder();
            while (!isEmpty()) {
                Boolean b = readBoolean();
                sb.append(b ? '1' : '0');
            }
            return sb.toString();
        }

        public byte readByte() {
            char c = readChar();
            return (byte) (c & 0xff);
        }
    }

    public final class BinaryOut {

        private BufferedOutputStream out; // the output stream
        private int buffer; // 8-bit buffer of bits to write out
        private int n; // number of bits remaining in buffer

        public BinaryOut(String filename) {
            try {
                OutputStream os = new FileOutputStream(filename);
                out = new BufferedOutputStream(os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeByte(int x) {
            assert x >= 0 && x < 256;

            // optimized if byte-aligned
            if (n == 0) {
                try {
                    out.write(x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // otherwise write one bit at a time
            for (int i = 0; i < 8; i++) {
                boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
                writeBit(bit);
            }
        }

        public void write(byte x) {
            writeByte(x & 0xff);
        }

        private void writeBit(boolean x) {
            // add bit to buffer
            buffer <<= 1;
            if (x)
                buffer |= 1;

            // if buffer is full (8 bits), write out as a single byte
            n++;
            if (n == 8)
                clearBuffer();
        }

        public void write(boolean x) {
            writeBit(x);
        }

        private void clearBuffer() {
            if (n == 0)
                return;
            if (n > 0)
                buffer <<= (8 - n);
            try {
                out.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            n = 0;
            buffer = 0;
        }

        public void flush() {
            clearBuffer();
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            flush();
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void encode(String inputFile, String outputFile, String freqFile) {
        // read file into string
        BinaryIn bin = new BinaryIn(inputFile);
        String s = bin.readString();

        // create frequency file
        Frequency freq = new Frequency();
        HashMap<Character, Integer> freqTable = freq.getFreq(s);
        System.out.println(freqTable); // debug
        freq.writeFreq(freqFile);

        // create tree and huffman code
        HuffmanCode huff = new HuffmanCode(freqTable);
        huff.encodeData(huff.root, "");
        System.out.println(huff.huffmanCode); // debug

        // encode data
        char[] input = s.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : input) {
            sb.append(huff.huffmanCode.get(c));
        }
        System.out.println(sb.toString()); // debug

        int paddingBits = 8 - (sb.length() % 8);
        if (paddingBits == 8) {
            paddingBits = 0;
        }

        for (int i = 0; i < paddingBits; i++) {
            sb.append('0');
        }

        // write data to encoded file
        BinaryOut bot = new BinaryOut(outputFile);
        bot.write((byte) paddingBits);
        for (char c : sb.toString().toCharArray()) {
            Boolean b = c == '0' ? false : true;
            bot.write(b);
        }
        bot.flush();
        bot.close();
    }

    public void decode(String inputFile, String outputFile, String freqFile) {
        // read encoded file into string
        BinaryIn bin = new BinaryIn(inputFile);
        int paddingBits = bin.readByte();
        String s = bin.readBits();

        // read in frequency file
        Frequency freq = new Frequency();
        freq.readFreq(freqFile);
        HashMap<Character, Integer> freqTable = freq.freq;
        System.out.println(freqTable); // debug

        // create tree and huffman code
        HuffmanCode huff = new HuffmanCode(freqTable);
        huff.encodeData(huff.root, "");
        System.out.println(huff.huffmanCode); // debug

        // decode data
        StringBuilder sb = new StringBuilder();
        if (huff.isLeaf(huff.root)) {
            while (huff.root.freq-- > 0) {
                sb.append(huff.root.ch);
            }
        } else {
            int index = -1;
            int endIndex = s.length() - paddingBits - 1;
            while (index < endIndex) {
                index = huff.decodeData(huff.root, index, s, sb);
            }
        }
        System.out.println(sb.toString()); // debug

        // write data to output file
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write(sb.toString());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HuffmanSubmit huffman = new HuffmanSubmit();
        huffman.encode("temp.txt", "ur.enc", "freq.txt");
        huffman.decode("ur.enc", "ur_dec.txt", "freq.txt");
        // After decoding, both ur.jpg and ur_dec.jpg should be the same.
        // On linux and mac, you can use `diff' command to check if they are the same.
    }
}
