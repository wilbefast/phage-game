/*
 Copyright (C) 2013 William James Dyce

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

import wjd.amb.control.EUpdateResult;
import wjd.amb.view.ICanvas;
import wjd.phage.level.Tile;

/**
 *
 * @author wdyce
 * @since Jan 14, 2013
 */
public class CivillianCell extends Unit
{
  /* METHODS */
  
  // constructors
  
  public CivillianCell(Tile tile)
  {
    super(tile);
  }
  
 
  @Override
  public void render(ICanvas canvas)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public EUpdateResult update(int t_delta)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  
  /* IMPLEMENTS -- UNIT */
  
  @Override
  public boolean isControllable()
  {
    return false;
  }

  @Override
  public void renderOrder(ICanvas canvas)
  {
    // not applicable
  }
  
  @Override
  public Type getType()
  {
    return Type.CIVILLIAN_CELL;
  }

}
