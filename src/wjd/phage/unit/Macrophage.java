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

/**
 *
 * @author wdyce
 * @since 14-01-13
 */
public class Macrophage extends Unit
{
  /* CONSTANTS */
  
  public static final float VIRUS_EAT_SPEED = 0.002f;
  
  /* METHODS */
  
  // constructors
  
  public Macrophage(Tile tile)
  {
    super(tile);
  }
  
  
  /* IMPLEMENTS -- IVISIBLE */
  @Override
  public void render(ICanvas canvas)
  {        
    canvas.setColour(Colour.WHITE);
    canvas.circle(position, Tile.SIZE.x/2, true);
    
    if(selected)
    {
      canvas.setLineWidth(3.0f);
      canvas.setColour(Colour.YELLOW);
      canvas.circle(position, Tile.SIZE.x/2, false);
    }
  }
  
  /* IMPLEMENTS -- IDYNAMIC */

  @Override
  public EUpdateResult update(int t_delta)
  {    
    // execute order
    if(order != null)
      return order.update(t_delta);
    
    // clear up infection if not moving
    else for(Tile t : tile.grid.getNeighbours(tile, true))
      t.getInfection().tryWithdraw(t_delta * VIRUS_EAT_SPEED);
    
    // All clear
    return EUpdateResult.CONTINUE;
  }

  /* OVERRIDES -- UNIT */
  
  @Override
  public boolean playerControlled()
  {
    return true;
  }
  
  @Override
  public void renderOverlay(ICanvas canvas)
  {
    // draw the order the unit is following (eg. its path) where applicable
    if(order != null)
      order.render(canvas);
  }
  
  /* IMPLEMENTS -- UNIT */

  @Override
  public EType getType()
  {
    return EType.MACROPHAGE;
  }
  
}
