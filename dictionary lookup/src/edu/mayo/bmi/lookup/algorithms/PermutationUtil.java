/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.lookup.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Mayo Clinic
 */
public class PermutationUtil
{
    /**
     * Gets all permutations for the given level and all sub-levels.
     * 
     * @param maxLevel
     */
    public static List getPermutationList(int maxLevel)
    {
        List permList = new ArrayList();
        for (int levelIdx = maxLevel; levelIdx >= 0; levelIdx--)
        {
            // contains ALL index values
            List baseNumList = new ArrayList();
            for (int j = 1; j <= levelIdx; j++)
            {
                baseNumList.add(new Integer(j));
            }

            Collection numListCol = new ArrayList();
            if (levelIdx != maxLevel)
            {
                numListCol.addAll(getNumLists(maxLevel, baseNumList));
            }
            else
            {
                numListCol.add(baseNumList);
            }

            Iterator numListItr = numListCol.iterator();
            while (numListItr.hasNext())
            {
                List numList = (List) numListItr.next();
                Collection pCol = PermutationUtil
                        .getLinearPermutations(numList);
                Iterator pItr = pCol.iterator();
                while (pItr.hasNext())
                {
                    List permutation = (List) pItr.next();
                    permList.add(permutation);
                }
            }

            if (levelIdx == 0)
            {
                permList.add(new ArrayList());
            }
        }

        return permList;
    }

    private static Collection getNumLists(int maxLevel, List baseNumList)
    {
        Collection numListCol = new ArrayList();
        buildPermutations(maxLevel, baseNumList, numListCol, new ArrayList(), 0);
        filterNonIncreasingLists(numListCol);
        return numListCol;
    }

    /**
     * Filters the number lists such that only lists with increasing numbers are
     * kept.
     * 
     * @param numListCol
     */
    private static void filterNonIncreasingLists(Collection numListCol)
    {
        Set removalSet = new HashSet();

        Iterator numListItr = numListCol.iterator();
        while (numListItr.hasNext())
        {
            List numList = (List) numListItr.next();
            Integer largestNum = null;
            Iterator numItr = numList.iterator();
            while (numItr.hasNext())
            {
                Integer num = (Integer) numItr.next();
                if (largestNum == null)
                {
                    largestNum = num;
                }
                else
                {
                    int comparison = largestNum.compareTo(num);
                    if (comparison == 1)
                    {
                        removalSet.add(numList);
                    }
                    else
                    {
                        largestNum = num;
                    }
                }
            }
        }
        numListCol.removeAll(removalSet);
    }

    /**
     * Recursively builds permutations of numbers specified by the base num
     * list. This includes permutations of these numbers with few items than the
     * original list.
     * 
     * @param maxLevel
     * @param baseNumList
     * @param numListCol
     * @param residualList
     * @param residualCount
     */
    private static void buildPermutations(
            int maxLevel,
            List baseNumList,
            Collection numListCol,
            List residualList,
            int residualCount)
    {
        if (residualCount > baseNumList.size())
        {
            return;
        }
        else if (residualCount == baseNumList.size())
        {
            numListCol.add(new ArrayList(residualList));
            return;
        }
        else
        {
            int num = ((Integer) baseNumList.get(residualCount)).intValue();
            residualCount++;
            for (int i = num; i <= maxLevel; i++)
            {
                List tempList = new ArrayList(residualList);
                if (!tempList.contains(new Integer(i)))
                {
                    tempList.add(new Integer(i));
                    buildPermutations(
                            maxLevel,
                            baseNumList,
                            numListCol,
                            tempList,
                            residualCount);
                }
            }
        }
    }

    /**
     * Gets a collection of lists, each list represents a single permutation.
     * This permutation is composed of Integer objects in defined order.
     * 
     * @param level
     * @return
     */
    public static Collection getLinearPermutations(List numList)
    {
        Collection permutations = new ArrayList();
        getLinearPermutations(permutations, new ArrayList(), numList);
        return permutations;
    }

    /**
     * Recurisvely builds permutations from the number list. The size of the
     * permutations remains constant.
     * 
     * @param permutations
     * @param plusList
     * @param numList
     */
    private static void getLinearPermutations(
            Collection permutations,
            List plusList,
            List numList)
    {
        Iterator numItr = numList.iterator();
        while (numItr.hasNext())
        {
            Integer num = (Integer) numItr.next();

            List subList = new ArrayList();
            subList.addAll(numList);
            subList.remove(num);

            plusList.add(num);

            if (subList.size() > 0)
            {
                getLinearPermutations(permutations, plusList, subList);
            }
            else
            {
                List permutation = new ArrayList();
                for (int i = 0; i < plusList.size(); i++)
                {
                    Integer n = (Integer) plusList.get(i);
                    permutation.add(n);
                }
                permutations.add(permutation);
            }

            plusList.remove(num);
        }
    }
}
