package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Modern JTable with striped rows, generous padding, and custom header rendering.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        initModernStyles();
    }

    private void initModernStyles() {
        setRowHeight(40);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFont(Theme.FONT_REGULAR);
        setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setFont(Theme.FONT_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBackground(Theme.bgCardAlt());
                label.setForeground(Theme.textSecondary());
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                        new EmptyBorder(0, 12, 0, 12)
                ));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });

        // Default cell padding
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBorder(new EmptyBorder(0, 12, 0, 12));
                label.setFont(Theme.FONT_REGULAR);

                if (isSelected) {
                    label.setBackground(Theme.tableSelectionBg());
                    label.setForeground(Theme.tableSelectionFg());
                } else {
                    label.setBackground(row % 2 == 0 ? Theme.tableRowEven() : Theme.tableRowOdd());
                    label.setForeground(Theme.textPrimary());
                }
                return label;
            }
        });

        Theme.addListener(isDark -> {
            setBackground(Theme.bgCard());
            getTableHeader().repaint();
            repaint();
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            c.setBackground(row % 2 == 0 ? Theme.tableRowEven() : Theme.tableRowOdd());
        }
        return c;
    }
}
