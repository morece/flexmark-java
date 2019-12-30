package com.vladsch.flexmark.ext.gfm.tasklist.internal;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItemPlacement;
import com.vladsch.flexmark.formatter.FormatterUtils;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.parser.ListOptions;
import com.vladsch.flexmark.util.ast.BlankLine;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class TaskListNodeFormatter implements NodeFormatter {
    private final FormatOptions formatOptions;
    private final ListOptions listOptions;

    public TaskListNodeFormatter(DataHolder options) {
        formatOptions = new FormatOptions(options);
        listOptions = ListOptions.get(options);
    }

    @Nullable
    @Override
    public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
        return new HashSet<>(Arrays.asList(
                new NodeFormattingHandler<>(TaskListItem.class, TaskListNodeFormatter.this::render),
                new NodeFormattingHandler<>(BulletList.class, TaskListNodeFormatter.this::render),
                new NodeFormattingHandler<>(OrderedList.class, TaskListNodeFormatter.this::render)
        ));
    }

    @Nullable
    @Override
    public Set<Class<?>> getNodeClasses() {
        return null;
    }

    private void render(TaskListItem node, NodeFormatterContext context, MarkdownWriter markdown) {
        if (context.isTransformingText()) {
            FormatterUtils.renderListItem(node, context, markdown, listOptions, node.getMarkerSuffix(), false);
        } else {
            BasedSequence markerSuffix = node.getMarkerSuffix();
            switch (formatOptions.taskListItemCase) {
                case AS_IS:
                    break;

                case LOWERCASE:
                    markerSuffix = markerSuffix.toLowerCase();
                    break;

                case UPPERCASE:
                    markerSuffix = markerSuffix.toUpperCase();
                    break;

                default:
                    throw new IllegalStateException("Missing case for TaskListItemCase " + formatOptions.taskListItemCase.name());
            }

            if (node.isItemDoneMarker()) {
                switch (formatOptions.taskListItemPlacement) {
                    case AS_IS:
                    case INCOMPLETE_FIRST:
                    case INCOMPLETE_NESTED_FIRST:
                        break;

                    case COMPLETE_TO_NON_TASK:
                    case COMPLETE_NESTED_TO_NON_TASK:
                        markerSuffix = markerSuffix.getEmptySuffix();
                        break;

                    default:
                        throw new IllegalStateException("Missing case for ListItemPlacement " + formatOptions.taskListItemPlacement.name());
                }
            }

            if (markerSuffix.isNotNull() && formatOptions.formatPrioritizedTaskItems) {
                node.setCanChangeMarker(false);
            }

            // task list item node overrides isParagraphWrappingDisabled which affects empty list item blank line rendering
            boolean forceLooseItem = node.isLoose() && (node.hasChildren() && node.getFirstChildAnyNot(BlankLine.class) != null);
            FormatterUtils.renderListItem(node, context, markdown, listOptions, markerSuffix.isEmpty() ? markerSuffix
                    : markerSuffix.getBuilder().append(markerSuffix).append(" ").append(markerSuffix.baseSubSequence(markerSuffix.getEndOffset() + 1, markerSuffix.getEndOffset() + 1)).toSequence(), forceLooseItem);
        }
    }

    private void render(BulletList node, NodeFormatterContext context, MarkdownWriter markdown) {
        renderList(node, context, markdown);
    }

    private void render(OrderedList node, NodeFormatterContext context, MarkdownWriter markdown) {
        renderList(node, context, markdown);
    }

    public static boolean hasIncompleteDescendants(Node node) {
        Node item = node.getFirstChild();
        while (item != null) {
            if (item instanceof TaskListItem) {
                if (!((TaskListItem) item).isItemDoneMarker()) {
                    return true;
                }
            }
            if (item instanceof Block && !(item instanceof Paragraph) && hasIncompleteDescendants(item)) {
                return true;
            }
            item = item.getNext();
        }

        return false;
    }

    public int taskItemPriority(Node node) {
        if (node instanceof TaskListItem) {
            if (((TaskListItem) node).isOrderedItem()) {
                return formatOptions.formatOrderedTaskItemPriority;
            } else {
                BasedSequence openingMarker = ((ListItem) node).getOpeningMarker();
                if (openingMarker.length() > 0) {
                    Integer priority = formatOptions.formatTaskItemPriorities.get(openingMarker.charAt(0));
                    if (priority != null) {
                        return priority;
                    } else {
                        return formatOptions.formatDefaultTaskItemPriority;
                    }
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    public int itemPriority(Node node) {
        Node item = node.getFirstChild();
        int priority = Integer.MIN_VALUE;

        if (node instanceof TaskListItem) {
            if (!((TaskListItem) node).isItemDoneMarker()) {
                priority = Math.max(priority, taskItemPriority(node));
            }
        }

        while (item != null) {
            if (item instanceof TaskListItem) {
                if (!((TaskListItem) item).isItemDoneMarker()) {
                    priority = Math.max(priority, taskItemPriority(item));
                }
            }
            if (item instanceof Block && !(item instanceof Paragraph)) {
                priority = Math.max(priority, itemPriority(item));
            }
            item = item.getNext();
        }

        return priority;
    }

    public void renderList(
            ListBlock node,
            NodeFormatterContext context,
            MarkdownWriter markdown
    ) {
        if (context.isTransformingText()) {
            context.renderChildren(node);
        } else {
            ArrayList<Node> itemList = new ArrayList<>();

            TaskListItemPlacement taskListItemPlacement = formatOptions.taskListItemPlacement;
            if (taskListItemPlacement != TaskListItemPlacement.AS_IS) {
                ArrayList<ListItem> incompleteTasks = new ArrayList<>();
                ArrayList<ListItem> completeItems = new ArrayList<>();
                boolean incompleteDescendants = taskListItemPlacement == TaskListItemPlacement.INCOMPLETE_NESTED_FIRST || taskListItemPlacement == TaskListItemPlacement.COMPLETE_NESTED_TO_NON_TASK;

                Node item = node.getFirstChild();
                while (item != null) {
                    if (item instanceof TaskListItem) {
                        TaskListItem taskItem = (TaskListItem) item;
                        if (!taskItem.isItemDoneMarker() || (incompleteDescendants && hasIncompleteDescendants(item))) {
                            incompleteTasks.add((ListItem) item);
                        } else {
                            completeItems.add((ListItem) item);
                        }
                    } else if (item instanceof ListItem) {
                        if (incompleteDescendants && hasIncompleteDescendants(item)) {
                            incompleteTasks.add((ListItem) item);
                        } else {
                            completeItems.add((ListItem) item);
                        }
                    }
                    item = item.getNext();
                }

                if (formatOptions.formatPrioritizedTaskItems) {
                    // have prioritized tasks
                    for (ListItem listItem : incompleteTasks) {
                        listItem.setPriority(itemPriority(listItem));
                    }

                    incompleteTasks.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
                    itemList.addAll(incompleteTasks);
                } else {
                    itemList.addAll(incompleteTasks);
                }

                itemList.addAll(completeItems);
            } else {
                Node item = node.getFirstChild();
                while (item != null) {
                    itemList.add(item);
                    item = item.getNext();
                }
            }

            FormatterUtils.renderList(node, context, markdown, itemList);
        }
    }

    public static class Factory implements NodeFormatterFactory {
        @NotNull
        @Override
        public NodeFormatter create(@NotNull DataHolder options) {
            return new TaskListNodeFormatter(options);
        }
    }
}
