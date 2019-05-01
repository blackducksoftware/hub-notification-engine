/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class DigestMessageContentProcessor extends MessageContentProcessor {
    private final MessageContentCollapser messageContentCollapser;

    @Autowired
    public DigestMessageContentProcessor(final MessageContentCollapser messageContentCollapser) {
        super(FormatType.DIGEST);
        this.messageContentCollapser = messageContentCollapser;
    }

    @Override
    public List<AggregateMessageContent> process(final List<AggregateMessageContent> messages) {
        final List<AggregateMessageContent> collapsedMessages = messageContentCollapser.collapse(messages);
        final Map<LinkableItem, List<AggregateMessageContent>> topicInfoToTopics = groupByTopic(collapsedMessages);

        final List<AggregateMessageContent> digestMessages = new ArrayList<>();
        for (final Map.Entry<LinkableItem, List<AggregateMessageContent>> topicInfoToTopicsEntry : topicInfoToTopics.entrySet()) {
            final LinkableItem topicInfo = topicInfoToTopicsEntry.getKey();
            final List<AggregateMessageContent> groupedMessages = topicInfoToTopicsEntry.getValue();

            final Set<LinkableItem> newSubTopics = gatherSubTopics(groupedMessages);
            final SortedSet<CategoryItem> newCategoryItems = gatherCategoryItems(groupedMessages);

            final AggregateMessageContent newMessage = new AggregateMessageContent(topicInfo.getName(), topicInfo.getValue(), topicInfo.getUrl().orElse(null), newSubTopics, newCategoryItems);
            digestMessages.add(newMessage);
        }

        return digestMessages;
    }

    private Map<LinkableItem, List<AggregateMessageContent>> groupByTopic(final List<AggregateMessageContent> messages) {
        final Map<LinkableItem, List<AggregateMessageContent>> groupedMessages = new LinkedHashMap<>();
        for (final AggregateMessageContent message : messages) {
            groupedMessages.computeIfAbsent(message, ignored -> new ArrayList<>()).add(message);
        }
        return groupedMessages;
    }

    private Set<LinkableItem> gatherSubTopics(final List<AggregateMessageContent> groupedMessages) {
        return groupedMessages
                   .stream()
                   .map(AggregateMessageContent::getSubTopics)
                   .flatMap(Set::stream)
                   .collect(Collectors.toSet());
    }

    private SortedSet<CategoryItem> gatherCategoryItems(final List<AggregateMessageContent> groupedMessages) {
        final List<CategoryItem> allCategoryItems = groupedMessages
                                                        .stream()
                                                        .map(AggregateMessageContent::getCategoryItems)
                                                        .flatMap(SortedSet::stream)
                                                        .collect(Collectors.toList());
        return combineCategoryItems(allCategoryItems);
    }

    private SortedSet<CategoryItem> combineCategoryItems(final List<CategoryItem> allCategoryItems) {
        // The amount of collapsing we do makes this impossible to map back to a single notification.
        final Long unknownNotificationId = Long.MIN_VALUE;
        final Map<CategoryKey, CategoryItem> keyToItems = new LinkedHashMap<>();
        for (final CategoryItem categoryItem : allCategoryItems) {
            final CategoryKey categoryKey = generateCategoryKey(categoryItem);
            final CategoryItem oldItem = keyToItems.get(categoryKey);

            final SortedSet<LinkableItem> linkableItems;
            if (null != oldItem) {
                linkableItems = combineLinkableItems(oldItem.getItems(), categoryItem.getItems());
            } else {
                linkableItems = categoryItem.getItems();
            }

            final CategoryItem newCategoryItem = new CategoryItem(categoryKey, categoryItem.getOperation(), unknownNotificationId, linkableItems);
            keyToItems.put(categoryKey, newCategoryItem);
        }
        return new TreeSet<>(keyToItems.values());
    }

    private CategoryKey generateCategoryKey(final CategoryItem categoryItem) {
        final List<String> keyParts = new ArrayList<>();
        keyParts.add(categoryItem.getOperation().name());
        for (final LinkableItem item : categoryItem.getItems()) {
            if (!item.isCollapsible()) {
                keyParts.add(item.getName());
                keyParts.add(item.getValue());
            }
        }

        return CategoryKey.from("digest", keyParts);
    }

    private SortedSet<LinkableItem> combineLinkableItems(final SortedSet<LinkableItem> oldItems, final SortedSet<LinkableItem> newItems) {
        final SortedSet<LinkableItem> combinedItems = new TreeSet<>(oldItems);
        newItems
            .stream()
            .filter(LinkableItem::isCollapsible)
            .forEach(combinedItems::add);
        return combinedItems;
    }

}
