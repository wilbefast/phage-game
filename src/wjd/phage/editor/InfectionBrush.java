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

/**
 *
 * @author wdyce
 * @since Nov 24, 2012
 */
public class InfectionBrush extends ABrush
{
  /* CONSTANTS */
  private static final float PAINT_SPEED = 0.1f;
  
  /* METHODS */
  public InfectionBrush()
  {
    super(true);
  }
  
  /* IMPLEMENTS -- ABRUSH */
  @Override
  public void paint(Tile target)
  {
    if(target.getType() == Tile.EType.FLOOR)
      target.getInfection().tryDeposit(PAINT_SPEED);
  }
  
  @Override
  public void erase(Tile target)
  {
    target.getInfection().empty();
  }

  @Override
  public void changeColour()
  {
    /* do nothing */
  }
  
  /* IMPLEMENTS -- IVISIBLE */

  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(Colour.GREEN);
    canvas.box(coverage, false);
  }
}
