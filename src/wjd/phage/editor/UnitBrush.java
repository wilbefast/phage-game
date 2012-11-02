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

import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.phage.level.Tile;
import wjd.phage.level.Unit;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public class UnitBrush extends ABrush
{
  /* ATTRIBUTES */
  private boolean erase = false;
  
  /* IMPLEMENTS -- IBRUSH */
  
  @Override
  public void paint(Tile target)
  {
    target.setUnit(erase ? null : new Unit(target));
  }

  @Override
  public void changeColour()
  {
    erase = !erase;
  }

  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(Colour.GREEN);
    canvas.circle(position, 10.0f);
  }
}
