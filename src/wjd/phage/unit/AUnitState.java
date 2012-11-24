/*
 Copyright (C) 2012 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wjd.phage.unit;

/**
 *
 * @author wdyce
 * @since Nov 23, 2012
 */
public abstract class AUnitState 
{
  /* INTERFACE */
  public abstract AUnitState update(Unit target, int t_delta);
  
  /* IMPLEMENTATIONS */
  
  /*public static AUnitState IDLING = new AUnitState()
  {
    @Override
    public AUnitState update(Unit u, int t_delta)
    {
      UnitOrder order = u.nextOrder();
      return (order == null) ? null : order.toState();
    }
  };
  
  public static AUnitState MOVING = new AUnitState()
  {
    @Override
    public AUnitState update(Unit u, int t_delta)
    {
      UnitOrder order = u.nextOrder();
      return (order == null) ? null : order.toState();
    }
  };*/

}
