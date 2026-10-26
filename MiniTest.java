package minitest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Item 1: Product with fields id, name, price, manufacturer and expDate.
 * Products are ordered by name (used by the BST).
 */
class Product implements Comparable<Product> {

    /** id of the dummy (empty) leaf nodes used by the BST. */
    public static final String EMPTY_ID = "0";

    private static final String[] NAMES = {"Laptop", "Smartphone", "Headphones", "Desk", "Backpack"};
    private static final String[] MANUFACTURERS = {"Acme", "Globex", "Initech", "Umbrella", "Soylent", "Hooli"};

    private final String id;
    private final String name;
    private final int price;
    private final String manufacturer;
    private final LocalDate expDate;

    public Product(String id, String name, int price, String manufacturer, LocalDate expDate) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.expDate = expDate;
    }

    /** Dummy node content used by the BST for an empty child slot. */
    public static Product createEmpty() {
        return new Product(EMPTY_ID, "", 0, "", null);
    }

    /**
     * Creates a random product. The name gets a random suffix so that names are
     * (almost always) unique and well spread, which keeps the BST reasonably balanced.
     */
    public static Product random(Random r) {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            id.append(r.nextInt(10));
        }
        StringBuilder name = new StringBuilder(NAMES[r.nextInt(NAMES.length)]).append('-');
        for (int i = 0; i < 8; i++) {
            name.append((char) ('a' + r.nextInt(26)));
        }
        int price = (r.nextInt(100) + 1) * 100;
        String manufacturer = MANUFACTURERS[r.nextInt(MANUFACTURERS.length)];
        LocalDate expDate = LocalDate.now().plusDays(30 + r.nextInt(1095));
        return new Product(id.toString(), name.toString(), price, manufacturer, expDate);
    }

    public boolean isEmpty() {
        return EMPTY_ID.equals(id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    @Override
    public int compareTo(Product other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[Empty Node]";
        }
        return id + " (" + name + ", Price: " + price + ", Manufacturer: " + manufacturer
                + ", Exp: " + expDate + ")";
    }
}

/**
 * Base binary tree (from the given code). Child index 0 = left, index 1 = right.
 * All searches use the product name as the key.
 */
class BinaryTree {

    protected DefaultMutableTreeNode root;

    public BinaryTree() {
        this.root = null;
    }

    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    /** A real node holds a Product that is not the BST's empty dummy. */
    protected static boolean isRealNode(DefaultMutableTreeNode node) {
        if (node == null) {
            return false;
        }
        Object obj = node.getUserObject();
        return (obj instanceof Product) && !((Product) obj).isEmpty();
    }

    /**
     * Height of the tree counted in levels (a single root = 1, empty tree = 0).
     * BST dummy nodes are not counted. Iterative, so it is safe for deep trees.
     */
    public int getHeight() {
        if (!isRealNode(root)) {
            return 0;
        }
        int height = 0;
        Queue<DefaultMutableTreeNode> level = new LinkedList<>();
        level.add(root);
        while (!level.isEmpty()) {
            height++;
            int size = level.size();
            for (int i = 0; i < size; i++) {
                DefaultMutableTreeNode node = level.poll();
                for (int c = 0; c < node.getChildCount(); c++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(c);
                    if (isRealNode(child)) {
                        level.add(child);
                    }
                }
            }
        }
        return height;
    }

    public DefaultMutableTreeNode depthFirstSearch(String key) {
        return depthFirstSearchRecursive(root, key);
    }

    private DefaultMutableTreeNode depthFirstSearchRecursive(DefaultMutableTreeNode node, String key) {
        // Base case: this branch has no node
        if (node == null) {
            return null;
        }

        if (isRealNode(node) && ((Product) node.getUserObject()).getName().equals(key)) {
            return node;
        }

        // Left child (index 0)
        if (node.getChildCount() > 0) {
            DefaultMutableTreeNode foundLeft = depthFirstSearchRecursive((DefaultMutableTreeNode) node.getChildAt(0), key);
            if (foundLeft != null) {
                return foundLeft;
            }
        }

        // Right child (index 1)
        if (node.getChildCount() > 1) {
            DefaultMutableTreeNode foundRight = depthFirstSearchRecursive((DefaultMutableTreeNode) node.getChildAt(1), key);
            if (foundRight != null) {
                return foundRight;
            }
        }

        return null;
    }

    public DefaultMutableTreeNode breadthFirstSearch(String key) {
        if (root == null) {
            return null;
        }

        Queue<DefaultMutableTreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            DefaultMutableTreeNode current = queue.poll();

            if (isRealNode(current) && ((Product) current.getUserObject()).getName().equals(key)) {
                return current;
            }

            int childCount = current.getChildCount();
            if (childCount > 0) {
                queue.add((DefaultMutableTreeNode) current.getChildAt(0)); // left
            }
            if (childCount > 1) {
                queue.add((DefaultMutableTreeNode) current.getChildAt(1)); // right
            }
        }
        return null;
    }
}

/**
 * Binary search tree ordered by Product.name (Product.compareTo).
 * Empty child slots are filled with dummy nodes whose Product id is "0".
 */
class BST extends BinaryTree {

    public BST() {
        super();
    }

    public void addNode(DefaultMutableTreeNode newNode) {
        if (newNode == null || newNode.getUserObject() == null) {
            return;
        }
        Product newProduct = (Product) newNode.getUserObject();

        if (root == null) {
            root = newNode;
        } else {
            DefaultMutableTreeNode p = root;
            while (true) {
                Product currProduct = (Product) p.getUserObject();

                // Case 1: leaf (no children) -> create left/right children, one of them a dummy
                if (p.getChildCount() == 0) {
                    if (newProduct.compareTo(currProduct) < 0) {
                        p.add(newNode); // left child
                        p.add(new DefaultMutableTreeNode(Product.createEmpty())); // dummy right child
                    } else {
                        p.add(new DefaultMutableTreeNode(Product.createEmpty())); // dummy left child
                        p.add(newNode); // right child
                    }
                    break;
                } // Case 2: node already has 2 children -> go down
                else if (p.getChildCount() == 2) {
                    if (newProduct.compareTo(currProduct) < 0) {
                        DefaultMutableTreeNode leftChildNode = (DefaultMutableTreeNode) p.getChildAt(0);
                        Product leftProduct = (Product) leftChildNode.getUserObject();

                        // Replace the dummy if the slot is empty
                        if (leftProduct.isEmpty()) {
                            leftChildNode.setUserObject(newProduct);
                            break;
                        }
                        p = leftChildNode;
                    } else {
                        DefaultMutableTreeNode rightChildNode = (DefaultMutableTreeNode) p.getChildAt(1);
                        Product rightProduct = (Product) rightChildNode.getUserObject();

                        if (rightProduct.isEmpty()) {
                            rightChildNode.setUserObject(newProduct);
                            break;
                        }
                        p = rightChildNode;
                    }
                }
            }
        }
    }

    /** Binary search by product name. */
    public DefaultMutableTreeNode binarySearch(String key) {
        DefaultMutableTreeNode p = root;

        while (p != null) {
            Product currProduct = (Product) p.getUserObject();

            // Stop at a dead end or an empty dummy slot
            if (currProduct == null || currProduct.isEmpty()) {
                return null;
            }

            int compareResult = key.compareTo(currProduct.getName());

            if (compareResult == 0) {
                return p;
            } else if (compareResult < 0) {
                p = (p.getChildCount() > 0) ? (DefaultMutableTreeNode) p.getChildAt(0) : null;
            } else {
                p = (p.getChildCount() > 1) ? (DefaultMutableTreeNode) p.getChildAt(1) : null;
            }
        }
        return null;
    }
}

/**
 * Complete binary tree: nodes are added level by level, left to right.
 * The parent of the node at index i is the node at index (i - 1) / 2.
 */
class CBT extends BinaryTree {

    private final ArrayList<DefaultMutableTreeNode> nodeList;

    public CBT() {
        super();
        nodeList = new ArrayList<>();
    }

    public void addNode(DefaultMutableTreeNode newNode) {
        if (newNode == null || newNode.getUserObject() == null) {
            return;
        }
        nodeList.add(newNode);

        if (root == null) {
            root = newNode;
            return;
        }

        int currentIdx = nodeList.size() - 1;
        int parentIdx = (currentIdx - 1) / 2;
        DefaultMutableTreeNode parentNode = nodeList.get(parentIdx);
        parentNode.add(newNode);
    }

    public int getSize() {
        return nodeList.size();
    }
}

/**
 * MiniTest "CBT & BST's Performance" - JFrame Form (NetBeans GUI Builder).
 */
public class MiniTest extends javax.swing.JFrame {

    private static final double NOT_RECORDED = -1;

    // Search modes (index into searchAvg)
    private static final int CBT_DFS = 0;
    private static final int CBT_BFS = 1;
    private static final int BST_DFS = 2;
    private static final int BST_BFS = 3;
    private static final int BST_BS = 4;
    private static final String[] MODE_NAMES = {"CBT DFS", "CBT BFS", "BST DFS", "BST BFS", "BST BS"};

    private Product[] data;      // item 2
    private int[] samples;       // item 3 (indexes into data)
    private static CBT cbt;
    private static BST bst;

    private double cbtBuildTime = NOT_RECORDED;
    private double bstBuildTime = NOT_RECORDED;
    private final double[] searchAvg = new double[5];
    private int lastFoundCount;

    private final Random random = new Random();

    public MiniTest() {
        initComponents();
        clearSearchTimes();
    }

    // ------------------------------------------------------------------ helpers

    private void clearSearchTimes() {
        for (int i = 0; i < searchAvg.length; i++) {
            searchAvg[i] = NOT_RECORDED;
        }
    }

    private void setStatus(String text) {
        jLabel5.setText(text);
    }

    private static double secondsSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000_000.0;
    }

    /** Reads a positive integer from a text field, or shows a message and returns -1. */
    private int readPositiveInt(javax.swing.JTextField field, String label) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException e) {
            // fall through to the message below
        }
        JOptionPane.showMessageDialog(this, "Please enter a positive integer for " + label + ".",
                "Invalid input", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        return -1;
    }

    private boolean requireData() {
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please press Gen Product first.",
                    "No data", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isCbtMode(int mode) {
        return mode == CBT_DFS || mode == CBT_BFS;
    }

    private boolean treeReady(int mode) {
        return isCbtMode(mode) ? cbt != null : bst != null;
    }

    /** Average time (seconds) per search over all samples. Keys are the names of data[samples[i]]. */
    private double timeSearch(int mode) {
        String[] keys = new String[samples.length];
        for (int i = 0; i < samples.length; i++) {
            keys[i] = data[samples[i]].getName();
        }

        int found = 0;
        long start = System.nanoTime();
        for (String key : keys) {
            DefaultMutableTreeNode result = null;
            switch (mode) {
                case CBT_DFS:
                    result = cbt.depthFirstSearch(key);
                    break;
                case CBT_BFS:
                    result = cbt.breadthFirstSearch(key);
                    break;
                case BST_DFS:
                    result = bst.depthFirstSearch(key);
                    break;
                case BST_BFS:
                    result = bst.breadthFirstSearch(key);
                    break;
                case BST_BS:
                    result = bst.binarySearch(key);
                    break;
                default:
                    break;
            }
            if (result != null) {
                found++;
            }
        }
        double total = secondsSince(start);
        lastFoundCount = found;
        return total / keys.length;
    }

    /** Shared by the 5 search buttons (items 7-11). */
    private void runSearchButton(int mode) {
        if (!requireData()) {
            return;
        }
        if (samples == null) {
            JOptionPane.showMessageDialog(this, "Please press Gen Search Samples first.",
                    "No samples", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!treeReady(mode)) {
            JOptionPane.showMessageDialog(this,
                    "Please press " + (isCbtMode(mode) ? "Gen CBT" : "Gen BST") + " first.",
                    "No tree", JOptionPane.WARNING_MESSAGE);
            return;
        }
        searchAvg[mode] = timeSearch(mode);
        setStatus(String.format(Locale.US, "%s: avg %.9f seconds (found %d/%d)",
                MODE_NAMES[mode], searchAvg[mode], lastFoundCount, samples.length));
    }

    /** Item 12: builds the report text. Only recorded or doable items are shown. */
    private String buildReport() {
        // Searches that can be done (tree + samples exist) but were not run yet are run now.
        if (samples != null) {
            for (int mode = 0; mode < searchAvg.length; mode++) {
                if (treeReady(mode) && searchAvg[mode] == NOT_RECORDED) {
                    searchAvg[mode] = timeSearch(mode);
                }
            }
        }

        String eq = "==================================================";
        String dash = "--------------------------------------------------";
        StringBuilder sb = new StringBuilder();
        sb.append(eq).append('\n');
        sb.append("       PERFORMANCE REPORT SUMMARY").append('\n');
        sb.append("       (Data Size = ").append(data.length).append(")").append('\n');
        sb.append(eq).append('\n');

        boolean hasBuild = cbtBuildTime != NOT_RECORDED || bstBuildTime != NOT_RECORDED;
        if (cbtBuildTime != NOT_RECORDED) {
            sb.append(String.format(Locale.US, "CBT Construction Time : %.3f seconds%n", cbtBuildTime));
        }
        if (bstBuildTime != NOT_RECORDED) {
            sb.append(String.format(Locale.US, "BST Construction Time : %.3f seconds%n", bstBuildTime));
        }

        boolean hasSearch = false;
        for (double v : searchAvg) {
            if (v != NOT_RECORDED) {
                hasSearch = true;
            }
        }
        if (hasSearch) {
            if (hasBuild) {
                sb.append(dash).append('\n');
            }
            sb.append("       (Sample Size = ").append(samples.length).append(")").append('\n');
            String[] labels = {"CBT DFS Avg ", "CBT BFS Avg ", "BST DFS Avg ", "BST BFS Avg ", "BST BS  Avg "};
            for (int mode = 0; mode < searchAvg.length; mode++) {
                if (searchAvg[mode] != NOT_RECORDED) {
                    sb.append(String.format(Locale.US, "%s : %.9f seconds%n", labels[mode], searchAvg[mode]));
                }
            }
        }
        sb.append(eq).append('\n');
        return sb.toString();
    }

    /** @SuppressWarnings("unchecked") */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MiniTest - CBT & BST's Performance");

        jLabel1.setText("Number of Products:");

        jTextField1.setText("100000");

        jButton1.setText("Gen Product");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Number of Samples:");

        jTextField2.setText("100");

        jButton2.setText("Gen Search Samples");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setText("CBT Height (levels):");

        jTextField3.setEditable(false);

        jButton3.setText("Gen CBT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel4.setText("BST Height (levels):");

        jTextField4.setEditable(false);

        jButton4.setText("Gen BST");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("CBT DFS");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("CBT BFS");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("BST DFS");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("BST BFS");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("BST BS");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Report Sumary");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(55);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        jTextArea1.setRows(14);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel5.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton10)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton8)
                    .addComponent(jButton9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Item 2: Gen Product
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int n = readPositiveInt(jTextField1, "the number of products");
        if (n < 0) {
            return;
        }
        data = new Product[n];
        for (int i = 0; i < n; i++) {
            data[i] = Product.random(random);
        }
        // Everything built from the old data is now stale
        samples = null;
        cbt = null;
        bst = null;
        cbtBuildTime = NOT_RECORDED;
        bstBuildTime = NOT_RECORDED;
        clearSearchTimes();
        jTextField3.setText("");
        jTextField4.setText("");
        jTextArea1.setText("");
        setStatus("Generated " + n + " products into data[].");
    }//GEN-LAST:event_jButton1ActionPerformed

    // Item 3: Gen Search Samples
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (!requireData()) {
            return;
        }
        int k = readPositiveInt(jTextField2, "the number of samples");
        if (k < 0) {
            return;
        }
        samples = new int[k];
        for (int i = 0; i < k; i++) {
            samples[i] = random.nextInt(data.length);
        }
        clearSearchTimes();
        setStatus("Generated " + k + " random search indexes into samples[].");
    }//GEN-LAST:event_jButton2ActionPerformed

    // Item 5: Gen CBT
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (!requireData()) {
            return;
        }
        cbt = new CBT();
        long start = System.nanoTime();
        for (int i = 0; i < data.length; i++) {
            cbt.addNode(new DefaultMutableTreeNode(data[i]));
        }
        cbtBuildTime = secondsSince(start);
        jTextField3.setText(String.valueOf(cbt.getHeight()));
        searchAvg[CBT_DFS] = NOT_RECORDED;
        searchAvg[CBT_BFS] = NOT_RECORDED;
        setStatus(String.format(Locale.US, "CBT built in %.3f seconds.", cbtBuildTime));
    }//GEN-LAST:event_jButton3ActionPerformed

    // Item 6: Gen BST (ordered by name)
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (!requireData()) {
            return;
        }
        bst = new BST();
        long start = System.nanoTime();
        for (int i = 0; i < data.length; i++) {
            bst.addNode(new DefaultMutableTreeNode(data[i]));
        }
        bstBuildTime = secondsSince(start);
        jTextField4.setText(String.valueOf(bst.getHeight()));
        searchAvg[BST_DFS] = NOT_RECORDED;
        searchAvg[BST_BFS] = NOT_RECORDED;
        searchAvg[BST_BS] = NOT_RECORDED;
        setStatus(String.format(Locale.US, "BST built in %.3f seconds.", bstBuildTime));
    }//GEN-LAST:event_jButton4ActionPerformed

    // Item 7: CBT DFS
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        runSearchButton(CBT_DFS);
    }//GEN-LAST:event_jButton5ActionPerformed

    // Item 8: CBT BFS
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        runSearchButton(CBT_BFS);
    }//GEN-LAST:event_jButton6ActionPerformed

    // Item 9: BST DFS
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        runSearchButton(BST_DFS);
    }//GEN-LAST:event_jButton7ActionPerformed

    // Item 10: BST BFS
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        runSearchButton(BST_BFS);
    }//GEN-LAST:event_jButton8ActionPerformed

    // Item 11: BST BS
    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        runSearchButton(BST_BS);
    }//GEN-LAST:event_jButton9ActionPerformed

    // Item 12: Report Sumary
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        if (data == null) {
            jTextArea1.setText("No data yet. Please press Gen Product first.");
            return;
        }
        jTextArea1.setText(buildReport());
        jTextArea1.setCaretPosition(0);
    }//GEN-LAST:event_jButton10ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MiniTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MiniTest().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
