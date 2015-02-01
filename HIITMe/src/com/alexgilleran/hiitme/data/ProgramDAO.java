/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.data;

import java.util.List;

import com.alexgilleran.hiitme.model.ProgramMetaData;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Node;

public interface ProgramDAO {
	List<ProgramMetaData> getProgramList();

	Program getProgram(long programId, boolean skipCache);

	void replaceProgramNode(ProgramMetaData program, Node programNode);

	long saveProgram(Program program);
	
	void deleteProgram(long programId);
}
