/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.dictionary.lookup;

import java.util.Collection;

/**
 * Base impl for a MetaDataHit implementation.
 * 
 * @author Mayo Clinic
 */
public abstract class BaseMetaDataHitImpl implements MetaDataHit
{
    /**
     * Two MetaDataHits are equal if their Meta field name/value pairs
     * are equal.
     */
    // In that case, this code is broken.  Note that this can contain all of those, but that may not contain all of these
//    public boolean equals(MetaDataHit mdh)
//    {
//        // check names first
//        if (getMetaFieldNames().containsAll(mdh.getMetaFieldNames()))
//        {
//            // check values
//            if (getMetaFieldValues().containsAll(mdh.getMetaFieldValues()))
//            {
//                return true;
//            }
//        }
//
//        return false;
//    }

   /**
    * Two MetaDataHits are equal if their Meta field name/value pairs
    * are equal.
    */
    public boolean equals( final MetaDataHit mdh ) {
       // Still not great as two equal names could have swapped equal values, but fast if complete check isn't required
       if ( getMetaFieldNames().size() != mdh.getMetaFieldNames().size()
             || getMetaFieldValues().size() != mdh.getMetaFieldValues().size()
             // TODO add types to MetaDataHit
             || !getMetaFieldNames().containsAll( mdh.getMetaFieldNames() ) ) {
          return false;
       }
       final Collection<String> thisMetaFieldNames = (Collection<String>)getMetaFieldNames();
       for ( String name : thisMetaFieldNames ) {
          if ( !getMetaFieldValue( name ).equals( mdh.getMetaFieldValue( name ) ) ) {
             return false;
          }
       }
       return true;
    }


   // Added 12-17-2012 to increase duplicate filtering in DictionaryLookupAnnotator
   // TODO As far as I have seen, instances of MetaDataHit are immutable (and should be so annotated)
   // If MetaDataHit ever becomes mutable then the hashCode may need to be reset upon mutation
   private int _hashCode = Integer.MIN_VALUE;

   @Override
   public int hashCode() {
      if ( _hashCode == Integer.MIN_VALUE ) {
         _hashCode = 27 * getMetaFieldNames().hashCode() + getMetaFieldValues().hashCode();
      }
      return _hashCode;
   }

}
