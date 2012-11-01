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
package wjd.phage.play;

import wjd.amb.control.Controller;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.phage.level.LevelScene;
import wjd.phage.level.Tile;
import wjd.phage.level.Unit;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class PlayController extends Controller
{
  /* ATTRIBUTES */
  LevelScene level;
  
  /* METHODS */

  // constructors
  public PlayController(LevelScene level)
  {
    this.level = level;
  }
  
  /* OVERRIDES -- CONTROLLER */
  
  @Override
  public EUpdateResult processMouseClick(IInput.MouseClick event)
  {
    if(event.button == IInput.EMouseButton.LEFT)
    {
      if(event.state)
      {
        Tile tile = 
          level.getTile(level.getGridPos(event.input.getMousePosition()));

        Unit unit = tile.getUnit();
        
        if(unit != null)
          unit.setSelected(!unit.isSelected());
        else
          tile.setUnit(new Unit(tile));
      }
    }
    
    return EUpdateResult.CONTINUE;
  }
}
