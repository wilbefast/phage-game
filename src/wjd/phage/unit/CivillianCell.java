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
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.phage.level.Tile;
import wjd.util.BoundedValue;

/**
 *
 * @author wdyce
 * @since Jan 14, 2013
 */
public class CivillianCell extends Unit
{
  /* CONSTANTS */
  
  public static final float INFECTION_SPEED = 0.002f; 
                                          // 0.5 seconds if tile is fully infected
  
  /* ATTRIBUTES */
  
  private BoundedValue infection = new BoundedValue();

  
  /* METHODS */
  
  // constructors
  
  public CivillianCell(Tile tile)
  {
    super(tile);
  }
  
 
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(Colour.RED);
    canvas.circle(position, Tile.SIZE.x/2, true);
  }

  @Override
  public EUpdateResult update(int t_delta)
  {
    // become an infected cell
    infection.tryDeposit(tile.getInfection().balance() * INFECTION_SPEED * t_delta);
    if(infection.isFull())
      return EUpdateResult.REPLACE_ME;
    
    return EUpdateResult.CONTINUE;
  }

  /* OVERRIDES -- UNIT */

  @Override
  public void renderOverlay(ICanvas canvas)
  {
    //! TODO
  }
  
  @Override
  public Unit getReplacement()
  {
    return (infection.isFull() ? new InfectedCell(tile) : null);
  }
  
  /* IMPLEMENTS -- UNIT */
  
  @Override
  public Type getType()
  {
    return Type.CIVILLIAN_CELL;
  }

}
