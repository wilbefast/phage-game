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
package wjd.phage.editor;

import wjd.amb.view.IVisible;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.level.Tile;
import wjd.phage.level.TileGrid;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public abstract class ABrush implements IVisible
{
  /* ATTRIBUTES */
  private final boolean fill;
  private boolean pending_repaint = true;
  protected Rect coverage = new Rect(Tile.SIZE.clone());
  
  /* METHODS */
  
  // constructors
  protected ABrush(boolean fill)
  {
    this.fill = fill;
  }
  
  // mutators
  
  public void forceRepaint()
  {
    pending_repaint = true;
  }
  
  public void setPosition(V2 position)
  {
    // reset only if a change has occured
    if(position.x != coverage.x + coverage.w/2 
    || position.y != coverage.y + coverage.h/2)
    {
      coverage.centrePos(position);
      pending_repaint = true;
    }
  }
  
  public void setSize(float size)
  {
    // reset only if a change has occured
    if(size*Tile.SIZE.x != coverage.w 
    || size*Tile.SIZE.y != coverage.h)
    {
      coverage.size(Tile.SIZE).scale(size);
      pending_repaint = true;
    }
  }
  
  public void touch(TileGrid grid, boolean erase)
  {
    if(pending_repaint)
      pending_repaint = false;
    else
      return;
    
    // fill entire covered area
    if(fill)
    {
      TileGrid target_field = grid.createSubGrid(coverage);
      if(target_field != null) for(Tile target : target_field)
        touch(target, erase);
    }
    // only paint a single Tile
    else
    {
      Tile target = grid.pixelToTile(coverage.getCentre());
      if(target != null)
        touch(target, erase);
    }
  }
  
  /* INTERFACE */
  public abstract void paint(Tile target);
  public abstract void erase(Tile target);
  public abstract void changeColour();

  /* SUBROUTINES */
  private void touch(Tile target, boolean erase)
  {
    if(erase)
      erase(target);
    else
      paint(target);
  }
  
}


