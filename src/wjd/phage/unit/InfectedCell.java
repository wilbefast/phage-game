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
import wjd.util.Timer;

/**
 *
 * @author wdyce
 * @since Jan 14, 2013
 */
public class InfectedCell extends Unit
{
  /* ATTRIBUTES */
  
  private Timer spawn_virus = new Timer(1500); // 2 times in 3 seconds
  
  /* METHODS */
  
  // constructors
  
  public InfectedCell(Tile tile)
  {
    super(tile);
  }
  
  /* OERRIDES -- IDYNAMIC */

  @Override
  public EUpdateResult update(int t_delta)
  {
    // generate infection periodically
    if(spawn_virus.update(t_delta) == EUpdateResult.FINISHED)
      tile.getInfection().fill();
    
    return EUpdateResult.CONTINUE;
  }
  
  /* OVERRIDES -- IVISIBLE */
  
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(Colour.VIOLET);
    canvas.circle(position, Tile.SIZE.x/2, true);
  }
  
  /* OVERRIDES -- UNIT */

  @Override
  public void renderOverlay(ICanvas canvas)
  {
    // do nothing
  }
  
  /* IMPLEMENTS -- UNIT */

  @Override
  public Type getType()
  {
    return Type.INFECTED_CELL;
  }

}
