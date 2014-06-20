/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2014 Yan Cheng Cheok <yccheok@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.yccheok.jstock.engine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.yccheok.jstock.gui.Pair;

/**
 *
 * @author yccheok
 */
public class CodeBucketLists {
    public CodeBucketLists(int maxBucketSize) {
        if (maxBucketSize <= 0) {
            throw new java.lang.IllegalArgumentException();
        }
        
        this.maxBucketSize = maxBucketSize;
    }
    
    public boolean add(Code code) {
        final String id = getStockServerFactoriesId(code);
        
        BucketList<Code> bucketList = getBucketLists().get(id);
        
        if (bucketList == null) {
            bucketList = new BucketList(this.maxBucketSize);
            getBucketLists().put(id, bucketList);
        }
        
        final int beforeSize = bucketList.size();
        
        final boolean status = bucketList.add(code);
        if (status == false) {
            // Duplicated.
            return status;
        }
        
        final int afterSize = bucketList.size();
        
        assert(afterSize >= 1);
        assert(afterSize >= beforeSize);
        
        if (afterSize == beforeSize) {
            return true;
        }
        
        final Integer basedIndexInfosIndex = getBasedIndexInfosIndexMapping().get(id);
        final int basedIndex;
        
        if (basedIndexInfosIndex == null) {
            // id not found.
            
            final int basedIndexInfosSize = getBasedIndexInfos().size();
            
            getBasedIndexInfosIndexMapping().put(id, basedIndexInfosSize);            
            
            if (basedIndexInfosSize == 0) {
                // ever first id in this CodeBucketLists.
                
                basedIndex = 0;
            } else {
                final Pair<String, Integer> previousBasedIndexInfo = getBasedIndexInfos().get(basedIndexInfosSize - 1);
                final BucketList<Code> previousBucketList = getBucketLists().get(previousBasedIndexInfo.first);
                final int previousBucketListSize = previousBucketList.size();
                basedIndex = previousBasedIndexInfo.second + previousBucketListSize;
            }  
            
            getBasedIndexInfos().add(Pair.create(id, basedIndex));
            
        } else {
            basedIndex = getBasedIndexInfos().get(basedIndexInfosIndex).second;
            
            for (int i = (basedIndexInfosIndex + 1), ei = getBasedIndexInfos().size(); i < ei; i++) {
                final Pair<String, Integer> basedIndexInfo = getBasedIndexInfos().get(i);
                getBasedIndexInfos().set(i, Pair.create(basedIndexInfo.first, basedIndexInfo.second + 1));
            }
        }
        
        getBucketListsIndexMapping().add(basedIndex, id);
        
        return true;
    }
    
    private String getStockServerFactoriesId(Code code) {
        List<StockServerFactory> stockServerFactories = Factories.INSTANCE.getStockServerFactories(code);
        
        StringBuilder stringBuilder = new StringBuilder();
        for (StockServerFactory stockServerFactory : stockServerFactories) {
            // Get simple name will be good enough at this moment
            stringBuilder.append(stockServerFactory.getClass().getSimpleName());
        }
        return stringBuilder.toString();
    }
    
    private final int maxBucketSize;
    
    /*        -----------------
     * "A" => | 0 | 1 | 2 | 3 |
     *        -----------------
     * "B" => | 4 |
     *        ---------
     * "C" => | 5 | 6 | 
     *        --------- 
     */
    private final Map<String, BucketList<Code>> bucketLists = new ConcurrentHashMap<String, BucketList<Code>>();
    
    /*
     * -------------------------------------------
     * | "A" | "A" | "A" | "A" | "B" | "C" | "C" |
     * -------------------------------------------
     */
    private final List<String> bucketListsIndexMapping = new java.util.concurrent.CopyOnWriteArrayList<String>();
    
    /*        
     * "A" => 0
     * "B" => 1
     * "C" => 2
     */    
    private final Map<String, Integer> basedIndexInfosIndexMapping = new ConcurrentHashMap<String, Integer>();
    
    /*
     * ----------------------------------
     * | ("A", 0) | ("B", 4) | ("C", 5) |
     * ----------------------------------
     */
    private final List<Pair<String, Integer>> basedIndexInfos = new java.util.concurrent.CopyOnWriteArrayList<Pair<String, Integer>>();

    /***************************************************************************
     * FOR UNIT TESTING PURPOSE
     **************************************************************************/
    
    /**
     * @return the bucketLists
     */
    public Map<String, BucketList<Code>> getBucketLists() {
        return java.util.Collections.unmodifiableMap(bucketLists);
    }

    /**
     * @return the bucketListsIndexMapping
     */
    public List<String> getBucketListsIndexMapping() {
        return java.util.Collections.unmodifiableList(bucketListsIndexMapping);
    }

    /**
     * @return the basedIndexInfosIndexMapping
     */
    public Map<String, Integer> getBasedIndexInfosIndexMapping() {
        return java.util.Collections.unmodifiableMap(basedIndexInfosIndexMapping);
    }

    /**
     * @return the basedIndexInfos
     */
    public List<Pair<String, Integer>> getBasedIndexInfos() {
        return java.util.Collections.unmodifiableList(basedIndexInfos);
    }    
}